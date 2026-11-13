package com.techchallenge.feedback.application.usecase;

import com.techchallenge.feedback.application.domain.Feedback;
import com.techchallenge.feedback.application.domain.WeeklyReport;
import com.techchallenge.feedback.application.port.input.GenerateWeeklyReportPort;
import com.techchallenge.feedback.application.port.output.AdminNotifierPort;
import com.techchallenge.feedback.application.port.output.DateTimeProviderPort;
import com.techchallenge.feedback.application.port.output.FeedbackPersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;

/** Responsabilidade única: consolidar as avaliações do período e enviar o relatório. */
@Slf4j
@RequiredArgsConstructor
public class GenerateWeeklyReportUseCase implements GenerateWeeklyReportPort {

  private final FeedbackPersistencePort feedbackPersistencePort;
  private final AdminNotifierPort adminNotifierPort;
  private final DateTimeProviderPort dateTimeProviderPort;

  @Override
  public WeeklyReport generate(int days) {
    LocalDate today = LocalDate.ofInstant(dateTimeProviderPort.now(), Feedback.BRAZIL_ZONE);
    List<LocalDate> period = WeeklyReport.periodEndingAt(today, days);

    // Uma consulta por dia no índice dia-index (sem varrer a tabela inteira).
    List<Feedback> feedbacks = period.stream()
        .flatMap(day -> feedbackPersistencePort.findBySentDay(day).stream())
        .toList();

    WeeklyReport report = WeeklyReport.of(period, feedbacks);
    adminNotifierPort.sendWeeklyReport(report);

    log.info("weekly report sent | start={} end={} total={} average={}",
        report.getStartDay(), report.getEndDay(), report.getTotal(), report.getAverageScore().orElse(null));
    return report;
  }
}
