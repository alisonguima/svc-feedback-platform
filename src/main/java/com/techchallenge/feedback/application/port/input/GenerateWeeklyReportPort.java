package com.techchallenge.feedback.application.port.input;

import com.techchallenge.feedback.application.domain.WeeklyReport;

public interface GenerateWeeklyReportPort {

  /** Consolida as avaliações dos últimos {@code days} dias (incluindo hoje) e envia aos administradores. */
  WeeklyReport generate(int days);
}
