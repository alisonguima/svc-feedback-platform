package com.techchallenge.feedback.adapter.input.lambda.exception;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.techchallenge.feedback.application.exception.InvalidFeedbackException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LambdaExceptionHandlerTest {

  @Test
  void invalidFeedbackBecomesBadRequestProblem() {
    APIGatewayProxyResponseEvent response = LambdaExceptionHandler.handle(new InvalidFeedbackException("nota inválida"));

    assertThat(response.getStatusCode()).isEqualTo(400);
    assertThat(response.getHeaders()).containsEntry("Content-Type", "application/problem+json; charset=utf-8");
    assertThat(response.getBody())
        .contains("\"status\":400", "\"title\":\"INVALID_FEEDBACK\"", "nota inválida", "\"timestamp\"");
  }

  @Test
  void unexpectedErrorBecomesInternalErrorWithoutLeakingDetails() {
    APIGatewayProxyResponseEvent response = LambdaExceptionHandler.handle(new RuntimeException("dynamodb fora"));

    assertThat(response.getStatusCode()).isEqualTo(500);
    assertThat(response.getBody()).contains("INTERNAL_ERROR").doesNotContain("dynamodb fora");
  }
}
