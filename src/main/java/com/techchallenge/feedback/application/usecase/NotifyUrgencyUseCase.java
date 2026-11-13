package com.techchallenge.feedback.application.usecase;

import com.techchallenge.feedback.application.domain.Feedback;
import com.techchallenge.feedback.application.port.input.NotifyUrgencyPort;
import com.techchallenge.feedback.application.port.output.AdminNotifierPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Responsabilidade única: avisar os administradores sobre avaliações críticas. */
@Slf4j
@RequiredArgsConstructor
public class NotifyUrgencyUseCase implements NotifyUrgencyPort {

  private final AdminNotifierPort adminNotifierPort;

  @Override
  public boolean notifyIfCritical(Feedback feedback) {
    if (!feedback.isCritical()) {
      log.info("feedback ignored (not critical) | id={} urgency={}", feedback.getId(), feedback.getUrgency());
      return false;
    }

    adminNotifierPort.notifyUrgency(feedback);
    log.info("urgency notification sent | id={}", feedback.getId());
    return true;
  }
}
