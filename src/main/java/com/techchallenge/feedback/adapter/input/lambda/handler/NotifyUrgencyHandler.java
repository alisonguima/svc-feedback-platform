package com.techchallenge.feedback.adapter.input.lambda.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.techchallenge.feedback.adapter.input.lambda.mapper.FeedbackStreamMapper;
import com.techchallenge.feedback.application.port.input.NotifyUrgencyPort;
import com.techchallenge.feedback.config.UseCaseConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;

/**
 * Função 2 - notificar-urgencia. Gatilho: stream do DynamoDB, já filtrado no
 * event source (INSERT + urgência CRITICAL). Se o envio falhar, a exceção sobe:
 * o Lambda refaz a tentativa e o alarme de erros avisa o administrador.
 */
@Slf4j
public class NotifyUrgencyHandler implements RequestHandler<DynamodbEvent, Map<String, Object>> {

  private final NotifyUrgencyPort notifyUrgencyPort;

  public NotifyUrgencyHandler() {
    this(UseCaseConfig.notifyUrgencyPort());
  }

  NotifyUrgencyHandler(NotifyUrgencyPort notifyUrgencyPort) {
    this.notifyUrgencyPort = notifyUrgencyPort;
  }

  @Override
  public Map<String, Object> handleRequest(DynamodbEvent event, Context context) {
    long sent = event.getRecords().stream()
        .map(DynamodbEvent.DynamodbStreamRecord::getDynamodb)
        .filter(Objects::nonNull)
        .map(streamRecord -> streamRecord.getNewImage())
        .filter(Objects::nonNull)
        .map(FeedbackStreamMapper::toDomain)
        .filter(notifyUrgencyPort::notifyIfCritical)
        .count();

    log.info("stream batch processed | records={} notificationsSent={}", event.getRecords().size(), sent);
    return Map.of("notificacoesEnviadas", sent);
  }
}
