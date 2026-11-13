package com.techchallenge.feedback.application.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UrgencyEnum {
  CRITICAL("CRÍTICA"),
  MEDIUM("MÉDIA"),
  LOW("BAIXA");

  private final String label;
}
