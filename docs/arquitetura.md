# Arquitetura

## Visão de nuvem

```mermaid
flowchart LR
    Aluno([Aluno / cliente]) -->|POST /avaliacao<br/>x-api-key| APIGW[API Gateway<br/>usage plan + throttling]
    APIGW --> L1[Lambda<br/>ReceiveFeedbackHandler]
    L1 -->|PutItem| DDB[(DynamoDB<br/>Avaliacoes + GSI dia-index)]
    DDB -->|Stream INSERT<br/>filtro: urgencia = CRITICAL| L2[Lambda<br/>NotifyUrgencyHandler]
    L2 -->|Publish| SNS[[SNS<br/>AdminTopic]]
    SCH[EventBridge Scheduler<br/>seg 08:00 America/Sao_Paulo] --> L3[Lambda<br/>WeeklyReportHandler]
    L3 -->|Query por dia| DDB
    L3 -->|Publish| SNS
    SNS -->|e-mail| Admin([Administrador])
    CW[CloudWatch<br/>logs, dashboard, alarmes] -->|alarme| SNS
    L1 -.logs.-> CW
    L2 -.logs.-> CW
    L3 -.logs.-> CW
    APIGW -.métricas.-> CW
```

## Visão hexagonal (por função)

```mermaid
flowchart LR
    subgraph input[adapter.input.lambda]
      H1[ReceiveFeedbackHandler]
      H2[NotifyUrgencyHandler]
      H3[WeeklyReportHandler]
    end
    subgraph app[application]
      P1([ReceiveFeedbackPort]) --> U1[ReceiveFeedbackUseCase]
      P2([NotifyUrgencyPort]) --> U2[NotifyUrgencyUseCase]
      P3([GenerateWeeklyReportPort]) --> U3[GenerateWeeklyReportUseCase]
      D[domain: Feedback, WeeklyReport]
      O1([FeedbackPersistencePort])
      O2([AdminNotifierPort])
      O3([DateTimeProviderPort])
    end
    subgraph output[adapter.output]
      A1[FeedbackDynamoDb]
      A2[SnsAdminNotifier]
      A3[SystemDateTimeProvider]
    end
    H1 --> P1
    H2 --> P2
    H3 --> P3
    U1 --> O1
    U1 --> O3
    U2 --> O2
    U3 --> O1
    U3 --> O2
    U3 --> O3
    O1 -.implementa.- A1
    O2 -.implementa.- A2
    O3 -.implementa.- A3
```

## Fluxos

1. **Recebimento:** a API chama o `ReceiveFeedbackHandler`, que converte o corpo HTTP e
   chama o `ReceiveFeedbackUseCase`. O domínio valida, classifica a urgência e o adapter
   do DynamoDB grava o item.
2. **Urgência:** cada gravação gera um evento no stream. O filtro do event source só
   entrega inserts com `urgencia = CRITICAL` ao `NotifyUrgencyHandler`, que chama o
   `NotifyUrgencyUseCase` e envia o e-mail pelo SNS.
3. **Relatório semanal:** o Scheduler aciona o `WeeklyReportHandler` toda segunda às 08:00.
   O `GenerateWeeklyReportUseCase` consulta o índice `dia-index` (uma Query por dia),
   consolida o `WeeklyReport` e envia o e-mail.
4. **Monitoramento:** logs em JSON com retenção de 7 dias, dashboard e alarmes. Qualquer
   alarme (erros das funções, 5xx da API) envia e-mail ao admin.
