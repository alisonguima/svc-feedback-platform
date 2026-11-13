package com.techchallenge.feedback.application.domain;

import com.techchallenge.feedback.application.domain.enums.UrgencyEnum;
import com.techchallenge.feedback.application.exception.InvalidFeedbackException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeedbackTest {

  private final Instant now = Instant.parse("2026-11-10T01:30:00Z"); // 09/11 22:30 em Brasília

  @Test
  void createsValidFeedbackWithUrgencyAndBrazilianDay() {
    Feedback feedback = Feedback.create("  Aula confusa  ", 2, now);

    assertThat(feedback.getId()).isNotBlank();
    assertThat(feedback.getDescription()).isEqualTo("Aula confusa");
    assertThat(feedback.getScore()).isEqualTo(2);
    assertThat(feedback.getUrgency()).isEqualTo(UrgencyEnum.CRITICAL);
    assertThat(feedback.getSentAt()).isEqualTo(now);
    assertThat(feedback.getSentDay()).isEqualTo(LocalDate.of(2026, 11, 9));
    assertThat(feedback.isCritical()).isTrue();
  }

  @ParameterizedTest
  @CsvSource({"0,CRITICAL", "3,CRITICAL", "4,MEDIUM", "6,MEDIUM", "7,LOW", "10,LOW"})
  void classifiesUrgencyByScore(int score, UrgencyEnum expected) {
    assertThat(Feedback.classify(score)).isEqualTo(expected);
  }

  @Test
  void nonCriticalFeedbackIsNotCritical() {
    assertThat(Feedback.create("Boa aula", 9, now).isCritical()).isFalse();
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void rejectsMissingDescription(String description) {
    assertThatThrownBy(() -> Feedback.create(description, 5, now))
        .isInstanceOf(InvalidFeedbackException.class)
        .hasMessageContaining("descricao")
        .extracting("code").isEqualTo("INVALID_FEEDBACK");
  }

  @Test
  void rejectsDescriptionLongerThanLimit() {
    String tooLong = "x".repeat(Feedback.MAX_DESCRIPTION_LENGTH + 1);

    assertThatThrownBy(() -> Feedback.create(tooLong, 5, now))
        .isInstanceOf(InvalidFeedbackException.class)
        .hasMessageContaining("500");
  }

  @Test
  void acceptsDescriptionAtTheLimit() {
    String atLimit = "x".repeat(Feedback.MAX_DESCRIPTION_LENGTH);

    assertThat(Feedback.create(atLimit, 5, now).getDescription()).hasSize(Feedback.MAX_DESCRIPTION_LENGTH);
  }

  @Test
  void rejectsMissingScore() {
    assertThatThrownBy(() -> Feedback.create("ok", null, now))
        .isInstanceOf(InvalidFeedbackException.class)
        .hasMessageContaining("nota");
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 11, 99})
  void rejectsScoreOutOfRange(int score) {
    assertThatThrownBy(() -> Feedback.create("ok", score, now))
        .isInstanceOf(InvalidFeedbackException.class)
        .hasMessageContaining("entre 0 e 10");
  }
}
