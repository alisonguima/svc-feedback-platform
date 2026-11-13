package com.techchallenge.feedback.application.exception;

public abstract class BaseException extends RuntimeException {

  private final String code;

  protected BaseException(String code, String message) {
    super(message);
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
