package com.techchallenge.feedback.application.port.input;

import com.techchallenge.feedback.application.domain.Feedback;

public interface ReceiveFeedbackPort {

  Feedback receive(String description, Integer score);
}
