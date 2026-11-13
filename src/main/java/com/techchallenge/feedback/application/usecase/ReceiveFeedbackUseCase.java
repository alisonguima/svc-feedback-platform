package com.techchallenge.feedback.application.usecase;

import com.techchallenge.feedback.application.domain.Feedback;
import com.techchallenge.feedback.application.port.input.ReceiveFeedbackPort;
import com.techchallenge.feedback.application.port.output.DateTimeProviderPort;
import com.techchallenge.feedback.application.port.output.FeedbackPersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Responsabilidade única: registrar a avaliação. Não envia e-mail — o aviso de
 * urgência é feito por outra função, acionada pelo stream da tabela.
 */
@Slf4j
@RequiredArgsConstructor
public class ReceiveFeedbackUseCase implements ReceiveFeedbackPort {

  private final FeedbackPersistencePort feedbackPersistencePort;
  private final DateTimeProviderPort dateTimeProviderPort;

  @Override
  public Feedback receive(String description, Integer score) {
    Feedback feedback = Feedback.create(description, score, dateTimeProviderPort.now());
    Feedback saved = feedbackPersistencePort.save(feedback);

    log.info("feedback registered | id={} score={} urgency={}",
        saved.getId(), saved.getScore(), saved.getUrgency());
    return saved;
  }
}
