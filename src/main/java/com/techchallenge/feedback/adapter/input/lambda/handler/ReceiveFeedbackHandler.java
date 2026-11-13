package com.techchallenge.feedback.adapter.input.lambda.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.techchallenge.feedback.adapter.input.lambda.dto.FeedbackRequest;
import com.techchallenge.feedback.adapter.input.lambda.dto.FeedbackResponse;
import com.techchallenge.feedback.adapter.input.lambda.exception.LambdaExceptionHandler;
import com.techchallenge.feedback.adapter.input.lambda.mapper.FeedbackRequestParser;
import com.techchallenge.feedback.adapter.input.lambda.response.ApiResponses;
import com.techchallenge.feedback.application.domain.Feedback;
import com.techchallenge.feedback.application.port.input.ReceiveFeedbackPort;
import com.techchallenge.feedback.config.UseCaseConfig;

// receber-avaliacao: POST /avaliacao
public class ReceiveFeedbackHandler
    implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  private final ReceiveFeedbackPort receiveFeedbackPort;

  public ReceiveFeedbackHandler() {
    this(UseCaseConfig.receiveFeedbackPort());
  }

  ReceiveFeedbackHandler(ReceiveFeedbackPort receiveFeedbackPort) {
    this.receiveFeedbackPort = receiveFeedbackPort;
  }

  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
    try {
      FeedbackRequest body = FeedbackRequestParser.parse(request.getBody());
      Feedback feedback = receiveFeedbackPort.receive(body.descricao(), body.nota());

      return ApiResponses.created(new FeedbackResponse(
          feedback.getId(), feedback.getUrgency().name(), feedback.getSentAt().toString()));
    } catch (Exception ex) {
      return LambdaExceptionHandler.handle(ex);
    }
  }
}
