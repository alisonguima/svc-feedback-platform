# svc-feedback-platform

Tech Challenge - Fase 4 (Pós Tech). Plataforma serverless de feedback de aulas:
alunos avaliam as aulas por uma API, avaliações críticas disparam e-mail na hora
pro admin, e toda segunda um relatório da semana (com média das notas) sai
sozinho. Tudo na AWS, serverless (Lambda + API Gateway + DynamoDB + SNS +
EventBridge Scheduler + CloudWatch), seguindo arquitetura hexagonal igual os
projetos anteriores da fase.

> **Vídeo de demonstração:** _(link)_
> **Autor:** Alison Guimarães (RM371602)

## Stack

| Categoria       | Tecnologia                                              |
| --------------- | ------------------------------------------------------- |
| Linguagem       | Java 21                                                 |
| Runtime         | AWS Lambda (`java21`, arm64)                            |
| API             | API Gateway (REST), com chave de API e usage plan       |
| Banco           | DynamoDB (on-demand) + Streams                          |
| Mensageria      | SNS (e-mail pro admin)                                  |
| Agendamento     | EventBridge Scheduler                                   |
| Observabilidade | CloudWatch (logs JSON, dashboard, alarmes)              |
| AWS SDK         | AWS SDK for Java v2                                     |
| IaC             | AWS SAM (`template.yaml`)                               |
| CI/CD           | GitHub Actions + OIDC                                   |
| Testes          | JUnit 5, Mockito, AssertJ, JaCoCo (gate 100% linha)     |

## Modelo de cloud

Nuvem pública (AWS), FaaS + serviços gerenciados: não tem servidor pra
administrar, escala sozinho e o custo é por uso. No volume de uma demo, tudo
fica dentro do free tier.

## Arquitetura

```
config          -> composition root (UseCaseConfig, AwsClientConfig, EnvironmentConfig)
adapter.input   -> lambda: handler (3 funcoes), dto, mapper (corpo HTTP / stream), response, exception
adapter.output  -> dynamodb (persistence, model, mapper), sns (notifier + formatter), time
application     -> domain (Feedback, WeeklyReport), port.input, port.output, usecase, exception
```

Domínio e use cases não dependem de AWS, Lambda nem Jackson (só recebem
`port.output` por injeção); os handlers só conhecem `port.input`; anotação de
DynamoDB fica isolada em `adapter.output.dynamodb.model`. Diagrama completo em
[`docs/arquitetura.md`](docs/arquitetura.md).

```
Aluno -POST /avaliacao-> API Gateway -> ReceiveFeedbackHandler -> DynamoDB (Avaliacoes)
                                                                      | stream (INSERT + CRITICAL)
                                                                      v
                                                    NotifyUrgencyHandler -> SNS -> e-mail admin
EventBridge Scheduler (seg 08:00) -> WeeklyReportHandler -> Query dia-index -> SNS -> e-mail admin
CloudWatch (alarmes de erro / 5xx) ------------------------------------------> SNS -> e-mail admin
```

### Decisões

- **Sem Spring.** Em Lambda isso só aumentaria cold start e memória sem ganhar
  nada pra três handlers pequenos. Quem faz esse papel é o `UseCaseConfig`
  (composition root manual) — cada função monta só os adapters que usa.
- **Um jar, três funções.** `mvn package` gera `target/feedback.jar` e cada
  Lambda aponta pro seu handler. A separação fica no código (handler + use
  case + porta por função) e na infra (role, gatilho e alarme por função).
- **Notificação pelo stream.** Quem recebe a avaliação só grava; quem avisa o
  admin é outra função acionada pelo stream do DynamoDB. Assim o recebimento
  não trava esperando o e-mail, e se o envio falhar o próprio Lambda tenta de
  novo.
- **SNS, não SES.** O e-mail do admin é só inscrito no tópico — não precisa
  verificar remetente nem sair do sandbox.
- **Nota não aceita coerção de tipo.** `"5"`, `5.5` ou `true` são recusados.

## Funções

### 1. `receber-avaliacao` — `ReceiveFeedbackHandler`
- Gatilho: API Gateway `POST /avaliacao` (header `x-api-key`).
- Entrada: `{"descricao": "Aula confusa", "nota": 2}` — descrição não vazia até
  500 caracteres (aceita `descrição` também), nota inteira de 0 a 10.
- Saída: `201 {"id", "urgencia", "dataEnvio"}`; erro em formato Problem Details
  (`400 INVALID_FEEDBACK`, `500 INTERNAL_ERROR`).
- Permissão: só `dynamodb:PutItem` na tabela.

### 2. `notificar-urgencia` — `NotifyUrgencyHandler`
- Gatilho: stream do DynamoDB, já filtrado no event source (`INSERT` +
  `urgencia = CRITICAL`).
- Ação: e-mail pro admin com descrição, urgência e data de envio (nota e id
  também vão junto).
- Permissão: leitura do stream (o SAM concede sozinho) + `sns:Publish`.
- Não grava nada. Se falhar o envio, o Lambda tenta de novo (até 2x) e o
  alarme de erro avisa o admin.

### 3. `gerar-relatorio` — `WeeklyReportHandler`
- Gatilho: EventBridge Scheduler, `cron(0 8 ? * MON *)` em
  `America/Sao_Paulo`.
- Ação: uma `Query` por dia no índice `dia-index` (7 dias incluindo hoje) e
  e-mail com a lista de avaliações, média das notas, quantidade por dia e
  quantidade por urgência.
- Permissão: `dynamodb:Query` só no índice `dia-index` + `sns:Publish`.
- Aceita `{"dias": N}` pra testar manualmente.

### Regra de urgência

O enunciado não fala como calcular urgência, então a regra abaixo foi decisão
nossa (`Feedback#classify`):

| Nota   | Urgência |
| ------ | -------- |
| 0 a 3  | CRITICAL (crítica) |
| 4 a 6  | MEDIUM (média)     |
| 7 a 10 | LOW (baixa)        |

## Segurança e governança de acesso

- Root com MFA e fora do uso diário; operação com usuário IAM próprio.
- Menor privilégio: cada função tem a sua role, só com o que usa de fato.
- API com chave (usage plan), throttling de 5 req/s (burst 10) e cota mensal.
- HTTPS obrigatório no API Gateway, criptografia em repouso no DynamoDB.
- Nada de segredo no repo: e-mail do admin entra via parâmetro de deploy, o
  pipeline autentica por OIDC (sem chave fixa), e a role de deploy só pode ser
  assumida pelo ambiente `producao` deste repositório.
- Avaliação é anônima — não guarda dado pessoal do aluno (LGPD).
- Budget alert na conta e tag `projeto=techchallenge-fase4` em tudo.

## Monitoramento e alertas

- Logs em JSON (formato padrão do Lambda), retenção de 7 dias.
- Dashboard `feedback-plataforma-monitoramento`: invocação, erro e duração por
  função, requisições/4xx/5xx da API, estado dos alarmes.
- Alarme (-> e-mail do admin) em erro de qualquer função e em 5xx da API.

## Rodando o projeto

### Pré-requisitos
- Java 21 e Maven 3.9+
- AWS CLI configurado (`aws sts get-caller-identity` precisa responder)
- AWS SAM CLI

### Build e testes
```bash
mvn verify
```
Roda os testes e o gate do JaCoCo (100% de cobertura de linha em domínio, use
cases, handlers e adapters — fora disso só boilerplate, ver exclusões no
`pom.xml`). Relatório em `target/site/jacoco/index.html`; o jar fica em
`target/feedback.jar`.

### Deploy manual
```bash
mvn package
sam deploy --parameter-overrides AdminEmail=seu-email@exemplo.com
```
Depois do primeiro deploy, confirma a inscrição no e-mail "AWS Notification -
Subscription Confirmation" — sem isso o SNS não entrega nada.

### Deploy automático (GitHub Actions)
1. Uma vez só, cria a role OIDC:
   ```bash
   aws cloudformation deploy --template-file infra/github-oidc.yaml --stack-name feedback-github-oidc \
     --capabilities CAPABILITY_NAMED_IAM --parameter-overrides GitHubOrg=SEU_USUARIO GitHubRepo=svc-feedback-platform
   aws cloudformation describe-stacks --stack-name feedback-github-oidc --query "Stacks[0].Outputs"
   ```
2. No GitHub: Settings -> Environments -> novo ambiente `producao`.
3. Settings -> Secrets and variables -> Actions: cria `AWS_ROLE_ARN` (saída
   acima) e `ADMIN_EMAIL`.
4. Push na `main` roda `mvn verify`, lint do template e deploy.

## Testando

Enviar avaliação (PowerShell; o script busca URL e API key sozinho):
```powershell
.\scripts\enviar-avaliacao.ps1 -Descricao "Aula confusa" -Nota 2     # critica, chega e-mail
.\scripts\enviar-avaliacao.ps1 -Descricao "Aula excelente" -Nota 9   # baixa
.\scripts\enviar-avaliacao.ps1 -Descricao "Invalida" -Nota 11        # 400
```

Disparar o relatório sem esperar segunda:
```bash
aws lambda invoke --function-name feedback-plataforma-gerar-relatorio \
  --payload '{"dias": 7}' --cli-binary-format raw-in-base64-out saida.json
```

Eventos de exemplo pro `sam local invoke` estão em `events/`.

## Custos

Tudo é cobrado por uso, no volume de uma demo fica dentro do free tier. Depois
de gravar a demonstração, derruba o ambiente:
```bash
sam delete
```
