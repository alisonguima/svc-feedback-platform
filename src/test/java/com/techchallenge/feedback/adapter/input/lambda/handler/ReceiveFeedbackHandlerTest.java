package com.techchallenge.feedback.adapter.input.lambda.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.techchallenge.feedback.application.exception.InvalidFeedbackException;
import com.techchallenge.feedback.application.port.input.ReceiveFeedbackPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.techchallenge.feedback.support.FeedbackFixture.critical;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceiveFeedbackHandlerTest {

  @Mock private ReceiveFeedbackPort receiveFeedbackPort;

  private ReceiveFeedbackHandler handler;

  @BeforeEach
  void setUp() {
    handler = new ReceiveFeedbackHandler(receiveFeedbackPort);
  }

  private APIGatewayProxyRequestEvent request(String body) {
    return new APIGatewayProxyRequestEvent().withHttpMethod("POST").withPath("/avaliacao").withBody(body);
  }

  @Test
  void returnsCreatedWithIdUrgencyAndSentAt() {
    when(receiveFeedbackPort.receive("Aula confusa", 2)).thenReturn(critical());

    APIGatewayProxyResponseEvent response =
        handler.handleRequest(request("{\"descricao\": \"Aula confusa\", \"nota\": 2}"), null);

    assertThat(response.getStatusCode()).isEqualTo(201);
    assertThat(response.getBody())
        .contains("\"id\":\"abc-123\"", "\"urgencia\":\"CRITICAL\"", "\"dataEnvio\":\"2026-11-09T15:30:00Z\"");
  }

  @Test
  void malformedBodyReturnsBadRequestWithoutCallingUseCase() {
    APIGatewayProxyResponseEvent response = handler.handleRequest(request("{nao-e-json"), null);

    assertThat(response.getStatusCode()).isEqualTo(400);
    verifyNoInteractions(receiveFeedbackPort);
  }

  @Test
  void domainValidationErrorReturnsBadRequest() {
    when(receiveFeedbackPort.receive(any(), any())).thenThrow(new InvalidFeedbackException("nota fora da faixa"));

    APIGatewayProxyResponseEvent response =
        handler.handleRequest(request("{\"descricao\": \"ok\", \"nota\": 11}"), null);

    assertThat(response.getStatusCode()).isEqualTo(400);
    assertThat(response.getBody()).contains("nota fora da faixa");
  }

  @Test
  void persistenceFailureReturnsInternalError() {
    when(receiveFeedbackPort.receive(any(), any())).thenThrow(new RuntimeException("dynamodb fora"));

    APIGatewayProxyResponseEvent response =
        handler.handleRequest(request("{\"descricao\": \"ok\", \"nota\": 8}"), null);

    assertThat(response.getStatusCode()).isEqualTo(500);
  }

  @Test
  void defaultConstructorWiresDependenciesFromEnvironment() {
    HandlerEnvironment.configure();

    assertThatCode(ReceiveFeedbackHandler::new).doesNotThrowAnyException();
  }
}
