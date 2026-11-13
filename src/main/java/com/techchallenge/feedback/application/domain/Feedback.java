package com.techchallenge.feedback.application.domain;

import com.techchallenge.feedback.application.domain.enums.UrgencyEnum;
import com.techchallenge.feedback.application.exception.InvalidFeedbackException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class Feedback {

  public static final ZoneId BRAZIL_ZONE = ZoneId.of("America/Sao_Paulo");

  public static final int MIN_SCORE = 0;
  public static final int MAX_SCORE = 10;
  public static final int MAX_DESCRIPTION_LENGTH = 500;

  private static final int CRITICAL_MAX_SCORE = 3;
  private static final int MEDIUM_MAX_SCORE = 6;

  private String id;
  private String description;
  private Integer score;
  private UrgencyEnum urgency;
  private Instant sentAt;
  private LocalDate sentDay;

  public static Feedback create(String description, Integer score, Instant now) {
    String validDescription = validateDescription(description);
    int validScore = validateScore(score);

    return new Feedback(
        UUID.randomUUID().toString(),
        validDescription,
        validScore,
        classify(validScore),
        now,
        LocalDate.ofInstant(now, BRAZIL_ZONE));
  }

  // 0-3 critica, 4-6 media, 7-10 baixa (o enunciado nao definia a regra, essa foi a nossa)
  public static UrgencyEnum classify(int score) {
    if (score <= CRITICAL_MAX_SCORE) {
      return UrgencyEnum.CRITICAL;
    }
    if (score <= MEDIUM_MAX_SCORE) {
      return UrgencyEnum.MEDIUM;
    }
    return UrgencyEnum.LOW;
  }

  public boolean isCritical() {
    return urgency == UrgencyEnum.CRITICAL;
  }

  private static String validateDescription(String description) {
    if (description == null || description.isBlank()) {
      throw new InvalidFeedbackException("O campo 'descricao' é obrigatório e deve ser um texto não vazio.");
    }
    String trimmed = description.strip();
    if (trimmed.length() > MAX_DESCRIPTION_LENGTH) {
      throw new InvalidFeedbackException(
          "O campo 'descricao' deve ter no máximo %d caracteres.".formatted(MAX_DESCRIPTION_LENGTH));
    }
    return trimmed;
  }

  private static int validateScore(Integer score) {
    if (score == null) {
      throw new InvalidFeedbackException("O campo 'nota' é obrigatório e deve ser um número inteiro.");
    }
    if (score < MIN_SCORE || score > MAX_SCORE) {
      throw new InvalidFeedbackException(
          "O campo 'nota' deve estar entre %d e %d.".formatted(MIN_SCORE, MAX_SCORE));
    }
    return score;
  }
}
