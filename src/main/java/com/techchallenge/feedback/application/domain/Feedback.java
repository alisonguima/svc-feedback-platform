package com.techchallenge.feedback.application.domain;

import com.techchallenge.feedback.application.domain.enums.UrgencyEnum;
import com.techchallenge.feedback.application.exception.InvalidFeedbackException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Avaliação de uma aula enviada por um estudante. A avaliação é anônima: não
 * guardamos nenhum dado pessoal do aluno (minimização de dados / LGPD).
 */
@Getter
@AllArgsConstructor
public class Feedback {

  /** Fuso usado para agrupar avaliações por dia no relatório. */
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

  /**
   * Cria uma nova avaliação válida. {@code now} vem do relógio da aplicação
   * (DateTimeProviderPort); o dia de envio é calculado no fuso de Brasília.
   */
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

  /**
   * Regra de urgência (definida pela equipe, o enunciado não especifica):
   * 0 a 3 = CRÍTICA, 4 a 6 = MÉDIA, 7 a 10 = BAIXA.
   */
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
