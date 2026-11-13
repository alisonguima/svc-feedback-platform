package com.techchallenge.feedback.adapter.input.lambda.exception;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.techchallenge.feedback.adapter.input.lambda.response.ApiResponses;
import com.techchallenge.feedback.application.exception.InvalidFeedbackException;
import lombok.extern.slf4j.Slf4j;

/** Equivalente ao GlobalExceptionHandler dos serviços Spring, para a função HTTP. */
@Slf4j
public final class LambdaExceptionHandler {

  private LambdaExceptionHandler() {}

  public static APIGatewayProxyResponseEvent handle(Exception ex) {
    if (ex instanceof InvalidFeedbackException invalid) {
      log.info("feedback rejected | {}", invalid.getMessage());
      return ApiResponses.problem(400, invalid.getCode(), invalid.getMessage());
    }

    log.error("unexpected error while registering feedback", ex);
    return ApiResponses.problem(500, "INTERNAL_ERROR", "Erro interno ao registrar a avaliação.");
  }
}
