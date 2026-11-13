package com.techchallenge.feedback.adapter.input.lambda.handler;

import com.techchallenge.feedback.application.domain.WeeklyReport;
import com.techchallenge.feedback.application.port.input.GenerateWeeklyReportPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.techchallenge.feedback.support.FeedbackFixture.feedback;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeeklyReportHandlerTest {

  @Mock private GenerateWeeklyReportPort generateWeeklyReportPort;

  private final LocalDate today = LocalDate.of(2026, 11, 8);

  @Test
  void usesSevenDaysByDefaultAndReturnsSummary() {
    WeeklyReport report = WeeklyReport.of(List.of(today), List.of(feedback("a", 4, "2026-11-08")));
    when(generateWeeklyReportPort.generate(anyInt())).thenReturn(report);
    WeeklyReportHandler handler = new WeeklyReportHandler(generateWeeklyReportPort);

    Map<String, Object> summary = handler.handleRequest(null, null);
    handler.handleRequest(Map.of("dias", "nao-numero"), null);

    verify(generateWeeklyReportPort, org.mockito.Mockito.times(2)).generate(7);
    assertThat(summary)
        .containsEntry("inicio", "2026-11-08")
        .containsEntry("fim", "2026-11-08")
        .containsEntry("total", 1L)
        .containsEntry("media", "4.00");
  }

  @Test
  void acceptsCustomPeriodAndEmptyReport() {
    WeeklyReport empty = WeeklyReport.of(List.of(today), List.of());
    when(generateWeeklyReportPort.generate(3)).thenReturn(empty);
    Map<String, Object> input = new HashMap<>();
    input.put("dias", 3);

    Map<String, Object> summary = new WeeklyReportHandler(generateWeeklyReportPort).handleRequest(input, null);

    assertThat(summary).containsEntry("total", 0L).containsEntry("media", null);
  }

  @Test
  void defaultConstructorWiresDependenciesFromEnvironment() {
    HandlerEnvironment.configure();

    assertThatCode(WeeklyReportHandler::new).doesNotThrowAnyException();
  }
}
