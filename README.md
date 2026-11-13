# svc-feedback-platform

Plataforma **serverless** de feedback de aulas (Tech Challenge - Fase 4).
Estudantes avaliam as aulas por uma API; avaliações **críticas** geram e-mail
imediato para os administradores e, toda semana, um **relatório consolidado**
(com média das notas) é enviado automaticamente. Roda 100% na **AWS**
(Lambda + API Gateway + DynamoDB + SNS + EventBridge Scheduler + CloudWatch) e
segue **Arquitetura Hexagonal (Ports & Adapters)**, no mesmo padrão dos serviços
das fases anteriores.

> **Vídeo de demonstração:** _(link)_
> **Autor:** Alison Guimarães (RM371602)

## Requisitos da Fase 4 → onde cada um é atendido

| Requisito | Onde |
| --------- | ---- |
| Ambiente de nuvem com segurança e governança de acesso | IAM com uma role por função (menor privilégio), API com chave + throttling, HTTPS, criptografia em repouso, root com MFA, deploy via OIDC — ver [Segurança](#segurança-e-governança-de-acesso) |
| Componentes de suporte (bancos etc.) | DynamoDB `Avaliacoes` + índice `dia-index`, SNS, EventBridge Scheduler (`template.yaml`) |
| Deploy automatizado | AWS SAM + GitHub Actions (`.github/workflows/deploy.yml`) |
| Aplicação monitorada | CloudWatch: logs, dashboard e alarmes — ver [Monitoramento](#monitoramento-e-alertas) |
| Notificações automáticas para problemas críticos | Avaliação crítica → `NotifyUrgencyHandler`; falhas do sistema → alarmes do CloudWatch |
| Relatório semanal com média | `WeeklyReportHandler`, toda segunda às 08:00 (Brasília) |
| Mínimo de 2 funções, responsabilidade única | 3 funções, cada uma com um handler, um use case e uma role IAM própria |
| `POST /avaliacao` com `descricao` e `nota` (0 a 10) | `ReceiveFeedbackHandler` |
| E-mail de urgência: descrição, urgência, data de envio | `AdminMessageFormatter#urgency` |
| Relatório: descrição, urgência, data, qtd. por dia, qtd. por urgência | `AdminMessageFormatter#weeklyReport` |

## Stack técnica

| Categoria       | Tecnologia                                              |
| --------------- | ------------------------------------------------------- |
| Linguagem       | Java 21                                                 |
| Runtime         | AWS Lambda (`java21`, arm64)                            |
| API             | Amazon API Gateway (REST) com chave de API e usage plan |
| Banco de dados  | Amazon DynamoDB (on-demand) + DynamoDB Streams          |
| Mensageria      | Amazon SNS (e-mail para o administrador)                |
| Agendamento     | Amazon EventBridge Scheduler                            |
| Observabilidade | Amazon CloudWatch (logs JSON, dashboard, alarmes)       |
| AWS SDK         | AWS SDK for Java v2 (DynamoDB Enhanced Client, SNS, url-connection-client) |
| Boilerplate     | Lombok                                                  |
| IaC             | AWS SAM (`template.yaml`)                               |
| CI/CD           | GitHub Actions + OIDC                                   |
| Testes          | JUnit 5, Mockito, AssertJ, JaCoCo (gate 100% linha)     |

## Modelo de cloud

**Nuvem pública (AWS)**, no modelo **FaaS + serviços gerenciados (PaaS)**: não há
servidor para administrar, a escala é automática e o custo é por uso. No volume
de uma demonstração, todos os serviços ficam dentro dos limites gratuitos.

## Arquitetura

```
config          → composition root (UseCaseConfig, AwsClientConfig, EnvironmentConfig)
adapter.input   → lambda: handler (3 funções), dto, mapper (corpo HTTP / stream), response, exception
adapter.output  → dynamodb (persistence, model, mapper), sns (notifier + formatter), time
application     → domain (Feedback, WeeklyReport), port.input, port.output, usecase, exception
```

Regras de dependência: domínio e use cases não dependem de AWS, Lambda nem
Jackson (só recebem `port.output` por injeção); os handlers só conhecem
`port.input`; anotações do DynamoDB só existem em `adapter.output.dynamodb.model`.

Diagrama completo em [`docs/arquitetura.md`](docs/arquitetura.md).

```
Aluno ─POST /avaliacao─► API Gateway ─► ReceiveFeedbackHandler ─► DynamoDB (Avaliacoes)
                                                                      │ stream (INSERT + CRITICAL)
                                                                      ▼
                                                    NotifyUrgencyHandler ─► SNS ─► e-mail admin
EventBridge Scheduler (seg 08:00) ─► WeeklyReportHandler ─► Query dia-index ─► SNS ─► e-mail admin
CloudWatch (alarmes de erro / 5xx) ─────────────────────────────────────────► SNS ─► e-mail admin
```

### Decisões de projeto

- **Java puro, sem Spring.** Em Lambda, o container de injeção aumentaria o cold
  start e o consumo de memória sem benefício para três handlers pequenos. O papel
  do `UseCaseConfig` com `@Bean` dos serviços Spring é feito por um composition
  root manual (`config/UseCaseConfig`), e cada função monta só os adapters que usa.
- **Um único jar, três funções.** O `mvn package` gera `target/feedback.jar`; cada
  função Lambda aponta para o seu handler. A separação de responsabilidades está
  no código (handler + use case + porta por função) e na infraestrutura (role IAM,
  gatilho e alarme por função).
- **Notificação pelo stream do DynamoDB.** A função de recebimento só grava; quem
  avisa o admin é outra função, acionada pelo stream com filtro no event source.
  Assim o recebimento não depende do envio de e-mail, e uma falha no envio é
  reprocessada pelo próprio Lambda.
- **SNS em vez de SES.** O e-mail do admin é inscrito no tópico; não é preciso
  verificar remetente nem sair do sandbox do SES.
- **Sem coerção de tipos na API.** `"5"`, `5.5` ou `true` não são aceitos como nota.

## Funções

### 1. `receber-avaliacao` — `ReceiveFeedbackHandler`
- **Gatilho:** API Gateway `POST /avaliacao` (header `x-api-key`).
- **Entrada:** `{"descricao": "Aula confusa", "nota": 2}`
  - `descricao`: texto não vazio, até 500 caracteres (também aceita `descrição`).
  - `nota`: inteiro de 0 a 10.
- **Saída:** `201 {"id", "urgencia", "dataEnvio"}`; erros no formato Problem
  Details (`400 INVALID_FEEDBACK`, `500 INTERNAL_ERROR`).
- **Permissões:** apenas `dynamodb:PutItem` na tabela.

### 2. `notificar-urgencia` — `NotifyUrgencyHandler`
- **Gatilho:** stream do DynamoDB, filtrado no event source (`INSERT` e `urgencia = CRITICAL`).
- **Ação:** e-mail ao admin com **descrição, urgência e data de envio** (e também nota e id).
- **Permissões:** leitura do stream (concedida pelo SAM) e `sns:Publish` no tópico do admin.
- Não grava no banco. Se o envio falhar, o Lambda refaz a tentativa (até 2 vezes)
  e o alarme de erros avisa o admin.

### 3. `gerar-relatorio` — `WeeklyReportHandler`
- **Gatilho:** EventBridge Scheduler `cron(0 8 ? * MON *)`, fuso `America/Sao_Paulo`.
- **Ação:** uma `Query` por dia no índice `dia-index` (7 dias, incluindo hoje) e
  e-mail com a lista das avaliações (descrição, urgência, nota, data),
  **média das notas**, **quantidade por dia** e **quantidade por urgência**.
- **Permissões:** `dynamodb:Query` apenas no índice `dia-index` e `sns:Publish`.
- Aceita `{"dias": N}` para testes manuais.

### Regra de urgência

O enunciado não define como calcular a urgência; a regra adotada fica em
`Feedback#classify`:

| Nota   | Urgência |
| ------ | -------- |
| 0 a 3  | `CRITICAL` (CRÍTICA) |
| 4 a 6  | `MEDIUM` (MÉDIA)     |
| 7 a 10 | `LOW` (BAIXA)        |

## Segurança e governança de acesso

- Usuário **root com MFA** e sem uso no dia a dia; operação com usuário IAM próprio.
- **Menor privilégio:** cada função tem sua própria role, só com as ações que usa.
- **API protegida por chave** (usage plan), com throttling (5 req/s, burst 10) e cota mensal.
- **HTTPS** obrigatório no API Gateway e **criptografia em repouso** no DynamoDB.
- **Sem segredos no repositório:** o e-mail do admin entra como parâmetro de deploy;
  o pipeline usa **OIDC** (sem chaves de acesso fixas). A role de deploy só pode ser
  assumida pelo ambiente `producao` deste repositório.
- **LGPD / minimização:** a avaliação é anônima; nenhum dado pessoal do aluno é coletado.
- Alerta de orçamento (AWS Budgets) e tag `projeto=techchallenge-fase4` em todos os recursos.

## Monitoramento e alertas

- **Logs** estruturados (formato JSON do Lambda) com retenção de 7 dias.
- **Dashboard** `feedback-plataforma-monitoramento`: invocações, erros e duração por
  função; requisições, 4xx e 5xx da API; estado dos alarmes.
- **Alarmes** com e-mail ao admin: erros em cada função e 5xx na API.

## Configuração e execução

### Pré-requisitos
- Java 21 e Maven 3.9+
- AWS CLI configurado (`aws sts get-caller-identity` deve responder)
- AWS SAM CLI

### Build e testes
```bash
mvn verify
```
Executa os testes e aplica o gate do JaCoCo: **100% de cobertura de linha** sobre
domínio, use cases, handlers e adapters. Ficam fora do gate apenas classes de
boilerplate (DTOs, enums, item do DynamoDB e configuração de wiring) — ver
exclusões no `pom.xml`. Relatório: `target/site/jacoco/index.html`.

O jar das funções fica em `target/feedback.jar`.

### Deploy
```bash
mvn package
sam deploy --parameter-overrides AdminEmail=seu-email@exemplo.com
```
Depois do primeiro deploy, abra o e-mail **"AWS Notification - Subscription
Confirmation"** e clique em **Confirm subscription**; sem isso o SNS não entrega
nenhuma mensagem.

### Deploy automatizado (GitHub Actions)
1. Uma única vez, crie a role OIDC:
   ```bash
   aws cloudformation deploy --template-file infra/github-oidc.yaml --stack-name feedback-github-oidc \
     --capabilities CAPABILITY_NAMED_IAM --parameter-overrides GitHubOrg=SEU_USUARIO GitHubRepo=svc-feedback-platform
   aws cloudformation describe-stacks --stack-name feedback-github-oidc --query "Stacks[0].Outputs"
   ```
2. No GitHub: *Settings → Environments → New environment* com o nome **producao**.
3. *Settings → Secrets and variables → Actions*: crie `AWS_ROLE_ARN` (saída acima) e `ADMIN_EMAIL`.
4. Todo push na `main` roda `mvn verify`, o lint do template e o deploy.

## Como testar

Envio de avaliações (PowerShell; o script busca a URL e a chave de API sozinho):
```powershell
.\scripts\enviar-avaliacao.ps1 -Descricao "Aula confusa" -Nota 2     # CRÍTICA: chega e-mail
.\scripts\enviar-avaliacao.ps1 -Descricao "Aula excelente" -Nota 9   # BAIXA
.\scripts\enviar-avaliacao.ps1 -Descricao "Inválida" -Nota 11        # 400
```

Disparar o relatório sem esperar a segunda-feira:
```bash
aws lambda invoke --function-name feedback-plataforma-gerar-relatorio \
  --payload '{"dias": 7}' --cli-binary-format raw-in-base64-out saida.json
```

Eventos de exemplo para `sam local invoke` estão em `events/`.

## Custos

Tudo é cobrado por uso e, no volume de uma demonstração, fica dentro dos limites
gratuitos. Após gravar a demonstração, remova o ambiente:
```bash
sam delete
```
