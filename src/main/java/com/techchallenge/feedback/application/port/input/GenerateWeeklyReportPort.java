package com.techchallenge.feedback.application.port.input;

import com.techchallenge.feedback.application.domain.WeeklyReport;

public interface GenerateWeeklyReportPort {

  WeeklyReport generate(int days);
}
