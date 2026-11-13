package com.techchallenge.feedback.application.exception;

public class InvalidFeedbackException extends BaseException {

  public InvalidFeedbackException(String message) {
    super("INVALID_FEEDBACK", message);
  }
}
