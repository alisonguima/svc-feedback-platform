package com.techchallenge.feedback.application.domain;

import com.techchallenge.feedback.application.domain.enums.UrgencyEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.techchallenge.feedback.support.FeedbackFixture.feedback;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeeklyReportTest {

  private final LocalDate today = LocalDate.of(2026, 11, 8);

  @Test
  void periodHasSevenDaysEndingToday() {
    List<LocalDate> period = WeeklyReport.periodEndingAt(today, 7);

    assertThat(period).hasSize(7).startsWith(LocalDate.of(2026, 11, 2)).endsWith(today);
  }

  @Test
  void rejectsPeriodShorterThanOneDay() {
    assertThatThrownBy(() -> WeeklyReport.periodEndingAt(today, 0))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void consolidatesAverageAndCounts() {
    List<LocalDate> period = WeeklyReport.periodEndingAt(today, 3);
    List<Feedback> feedbacks = List.of(
        feedback("b", 8, "2026-11-08"),
        feedback("a", 2, "2026-11-06"),
        feedback("c", 5, "2026-11-08"));

    WeeklyReport report = WeeklyReport.of(period, feedbacks);

    assertThat(report.getStartDay()).isEqualTo(LocalDate.of(2026, 11, 6));
    assertThat(report.getEndDay()).isEqualTo(today);
    assertThat(report.getTotal()).isEqualTo(3);
    assertThat(report.getAverageScore()).contains(new BigDecimal("5.00"));
    assertThat(report.getCountByUrgency())
        .containsExactly(Map.entry(UrgencyEnum.CRITICAL, 1L), Map.entry(UrgencyEnum.MEDIUM, 1L),
            Map.entry(UrgencyEnum.LOW, 1L));
    assertThat(report.getCountByDay()).containsExactly(
        Map.entry(LocalDate.of(2026, 11, 6), 1L),
        Map.entry(LocalDate.of(2026, 11, 7), 0L),
        Map.entry(today, 2L));
    assertThat(report.getFeedbacks()).extracting(Feedback::getId).containsExactly("a", "b", "c");
  }

  @Test
  void averageRoundsToTwoDecimals() {
    List<LocalDate> period = List.of(today);
    WeeklyReport report = WeeklyReport.of(period, List.of(
        feedback("a", 1, "2026-11-08"), feedback("b", 2, "2026-11-08"), feedback("c", 2, "2026-11-08")));

    assertThat(report.getAverageScore()).contains(new BigDecimal("1.67"));
  }

  @Test
  void emptyPeriodHasNoAverage() {
    WeeklyReport report = WeeklyReport.of(List.of(today), List.of());

    assertThat(report.getTotal()).isZero();
    assertThat(report.getAverageScore()).isEmpty();
    assertThat(report.getCountByUrgency()).containsValues(0L, 0L, 0L);
  }
}
