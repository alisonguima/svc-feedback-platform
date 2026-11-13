package com.techchallenge.feedback.application.port.input;

import com.techchallenge.feedback.application.domain.Feedback;

public interface ReceiveFeedbackPort {

  /** Valida, classifica a urgência e registra uma nova avaliação. */
  Feedback receive(String description, Integer score);
}
