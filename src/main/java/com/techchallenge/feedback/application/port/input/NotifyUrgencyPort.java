package com.techchallenge.feedback.application.port.input;

import com.techchallenge.feedback.application.domain.Feedback;

public interface NotifyUrgencyPort {

  /**
   * Avisa os administradores sobre uma avaliação crítica. Retorna {@code false}
   * quando a avaliação não é crítica e nada é enviado.
   */
  boolean notifyIfCritical(Feedback feedback);
}
