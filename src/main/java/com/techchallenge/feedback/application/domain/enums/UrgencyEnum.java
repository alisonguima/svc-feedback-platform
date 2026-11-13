package com.techchallenge.feedback.application.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Nível de urgência de uma avaliação. A regra de classificação por nota fica em
 * {@code Feedback#classify}; aqui só ficam os rótulos exibidos nos e-mails.
 */
@Getter
@RequiredArgsConstructor
public enum UrgencyEnum {
  CRITICAL("CRÍTICA"),
  MEDIUM("MÉDIA"),
  LOW("BAIXA");

  private final String label;
}
