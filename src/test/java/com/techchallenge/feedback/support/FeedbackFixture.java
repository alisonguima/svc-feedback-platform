package com.techchallenge.feedback.support;

import com.techchallenge.feedback.application.domain.Feedback;
import com.techchallenge.feedback.application.domain.enums.UrgencyEnum;

import java.time.Instant;
import java.time.LocalDate;

public final class FeedbackFixture {

  public static final Instant SENT_AT = Instant.parse("2026-11-09T15:30:00Z"); // 12:30 em Brasília

  private FeedbackFixture() {}

  public static Feedback feedback(String id, int score, String day) {
    return new Feedback(id, "Aula " + id, score, Feedback.classify(score),
        Instant.parse(day + "T15:00:00Z"), LocalDate.parse(day));
  }

  public static Feedback critical() {
    return new Feedback("abc-123", "Aula confusa", 2, UrgencyEnum.CRITICAL, SENT_AT, LocalDate.of(2026, 11, 9));
  }
}
