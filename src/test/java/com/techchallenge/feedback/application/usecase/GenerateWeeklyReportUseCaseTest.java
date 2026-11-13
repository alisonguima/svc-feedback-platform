package com.techchallenge.feedback.application.usecase;

import com.techchallenge.feedback.application.domain.WeeklyReport;
import com.techchallenge.feedback.application.port.output.AdminNotifierPort;
import com.techchallenge.feedback.application.port.output.DateTimeProviderPort;
import com.techchallenge.feedback.application.port.output.FeedbackPersistencePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static com.techchallenge.feedback.support.FeedbackFixture.feedback;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateWeeklyReportUseCaseTest {

  @Mock private FeedbackPersistencePort feedbackPersistencePort;
  @Mock private AdminNotifierPort adminNotifierPort;
  @Mock private DateTimeProviderPort dateTimeProviderPort;
  @InjectMocks private GenerateWeeklyReportUseCase useCase;

  @Test
  void queriesEachDayOfThePeriodAndSendsReport() {
    // 02:00 UTC de 09/11 ainda é 08/11 em Brasília
    when(dateTimeProviderPort.now()).thenReturn(Instant.parse("2026-11-09T02:00:00Z"));
    when(feedbackPersistencePort.findBySentDay(any())).thenReturn(List.of());
    when(feedbackPersistencePort.findBySentDay(LocalDate.of(2026, 11, 8)))
        .thenReturn(List.of(feedback("a", 4, "2026-11-08")));

    WeeklyReport report = useCase.generate(7);

    assertThat(report.getEndDay()).isEqualTo(LocalDate.of(2026, 11, 8));
    assertThat(report.getStartDay()).isEqualTo(LocalDate.of(2026, 11, 2));
    assertThat(report.getTotal()).isEqualTo(1);
    verify(feedbackPersistencePort, times(7)).findBySentDay(any());
    verify(adminNotifierPort).sendWeeklyReport(report);
  }
}
