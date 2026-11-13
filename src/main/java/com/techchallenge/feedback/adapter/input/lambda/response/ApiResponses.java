package com.techchallenge.feedback.adapter.input.lambda.response;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.feedback.adapter.input.lambda.dto.ProblemResponse;

import java.time.Instant;
import java.util.Map;

public final class ApiResponses {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final Map<String, String> JSON = Map.of("Content-Type", "application/json; charset=utf-8");
  private static final Map<String, String> PROBLEM_JSON =
      Map.of("Content-Type", "application/problem+json; charset=utf-8");

  private ApiResponses() {}

  public static APIGatewayProxyResponseEvent created(Object body) {
    return build(201, JSON, body);
  }

  public static APIGatewayProxyResponseEvent problem(int status, String title, String detail) {
    return build(status, PROBLEM_JSON, new ProblemResponse(status, title, detail, Instant.now().toString()));
  }

  // valueToTree em vez de writeValueAsString pra nao lidar com JsonProcessingException aqui
  private static APIGatewayProxyResponseEvent build(int status, Map<String, String> headers, Object body) {
    return new APIGatewayProxyResponseEvent()
        .withStatusCode(status)
        .withHeaders(headers)
        .withBody(MAPPER.valueToTree(body).toString());
  }
}
