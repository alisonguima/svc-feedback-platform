package com.techchallenge.feedback.adapter.input.lambda.mapper;

import com.techchallenge.feedback.adapter.input.lambda.dto.FeedbackRequest;
import com.techchallenge.feedback.application.exception.InvalidFeedbackException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeedbackRequestParserTest {

  @Test
  void parsesValidBody() {
    FeedbackRequest request = FeedbackRequestParser.parse("{\"descricao\": \"Aula confusa\", \"nota\": 2}");

    assertThat(request).isEqualTo(new FeedbackRequest("Aula confusa", 2));
  }

  @Test
  void acceptsDescriptionFieldWithAccent() {
    assertThat(FeedbackRequestParser.parse("{\"descrição\": \"ok\", \"nota\": 10}").descricao()).isEqualTo("ok");
  }

  @ParameterizedTest
  @ValueSource(strings = {"\"5\"", "5.5", "true", "99999999999"})
  void doesNotCoerceInvalidScoreTypes(String score) {
    assertThat(FeedbackRequestParser.parse("{\"descricao\": \"ok\", \"nota\": " + score + "}").nota()).isNull();
  }

  @Test
  void ignoresNonTextDescription() {
    assertThat(FeedbackRequestParser.parse("{\"descricao\": 123, \"nota\": 1}").descricao()).isNull();
  }

  @Test
  void missingFieldsBecomeNull() {
    assertThat(FeedbackRequestParser.parse("{}")).isEqualTo(new FeedbackRequest(null, null));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "{nao-e-json", "[1, 2]", "\"texto\""})
  void rejectsInvalidBody(String body) {
    assertThatThrownBy(() -> FeedbackRequestParser.parse(body))
        .isInstanceOf(InvalidFeedbackException.class)
        .hasMessageContaining("Corpo da requisição inválido");
  }
}
