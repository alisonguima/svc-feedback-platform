package com.techchallenge.feedback.adapter.input.lambda.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techchallenge.feedback.adapter.input.lambda.dto.FeedbackRequest;
import com.techchallenge.feedback.application.exception.InvalidFeedbackException;

import java.util.Optional;

/**
 * Converte o corpo HTTP em {@link FeedbackRequest} sem coerção de tipos: "5",
 * 5.5 ou true NÃO viram nota. Campos com tipo errado chegam como {@code null} e
 * são recusados pelas regras do domínio.
 */
public final class FeedbackRequestParser {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final String DESCRIPTION_FIELD = "descricao";
  private static final String DESCRIPTION_FIELD_WITH_ACCENT = "descrição";
  private static final String SCORE_FIELD = "nota";

  private FeedbackRequestParser() {}

  public static FeedbackRequest parse(String body) {
    JsonNode root = readObject(body);

    JsonNode description = Optional.ofNullable(root.get(DESCRIPTION_FIELD))
        .orElseGet(() -> root.get(DESCRIPTION_FIELD_WITH_ACCENT));
    JsonNode score = root.get(SCORE_FIELD);

    return new FeedbackRequest(
        Optional.ofNullable(description).filter(JsonNode::isTextual).map(JsonNode::asText).orElse(null),
        Optional.ofNullable(score).filter(FeedbackRequestParser::isInteger).map(JsonNode::intValue).orElse(null));
  }

  private static JsonNode readObject(String body) {
    try {
      return Optional.ofNullable(body)
          .filter(text -> !text.isBlank())
          .map(FeedbackRequestParser::readTree)
          .filter(JsonNode::isObject)
          .orElseThrow(FeedbackRequestParser::invalidBody);
    } catch (IllegalStateException ex) {
      throw invalidBody();
    }
  }

  private static JsonNode readTree(String body) {
    try {
      return MAPPER.readTree(body);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException(ex);
    }
  }

  private static boolean isInteger(JsonNode node) {
    return node.isIntegralNumber() && node.canConvertToInt();
  }

  private static InvalidFeedbackException invalidBody() {
    return new InvalidFeedbackException("Corpo da requisição inválido: envie um objeto JSON com 'descricao' e 'nota'.");
  }
}
