package com.techchallenge.feedback.adapter.output.sns.formatter;

import com.techchallenge.feedback.application.domain.Feedback;
import com.techchallenge.feedback.application.domain.WeeklyReport;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static com.techchallenge.feedback.support.FeedbackFixture.critical;
import static com.techchallenge.feedback.support.FeedbackFixture.feedback;
import static org.assertj.core.api.Assertions.assertThat;

class AdminMessageFormatterTest {

  private final LocalDate today = LocalDate.of(2026, 11, 8);

  @Test
  void urgencyEmailHasDescriptionUrgencyAndSentDate() {
    AdminMessage message = AdminMessageFormatter.urgency(critical());

    assertThat(message.subject()).isEqualTo("[URGENTE] Feedback critico recebido - nota 2");
    assertThat(message.subject()).isASCII().hasSizeLessThan(100);
    assertThat(message.body())
        .contains("Descrição : Aula confusa")
        .contains("Urgência  : CRÍTICA")
        .contains("09/11/2026 12:30:00 (horário de Brasília)");
  }

  @Test
  void weeklyReportHasAllRequiredData() {
    List<LocalDate> period = WeeklyReport.periodEndingAt(today, 2);
    WeeklyReport report = WeeklyReport.of(period, List.of(
        feedback("a", 2, "2026-11-08"), feedback("b", 9, "2026-11-08")));

    AdminMessage message = AdminMessageFormatter.weeklyReport(report);

    assertThat(message.subject()).isEqualTo("Relatorio semanal de feedbacks - 07/11/2026 a 08/11/2026");
    assertThat(message.subject()).isASCII();
    assertThat(message.body())
        .contains("Total de avaliações: 2")
        .contains("Média das notas: 5,50")
        .contains("  - CRÍTICA: 1", "  - MÉDIA: 0", "  - BAIXA: 1")
        .contains("  - 07/11/2026: 0", "  - 08/11/2026: 2")
        .contains("  - Aula a | CRÍTICA | 2 | 08/11/2026 12:00:00");
  }

  @Test
  void emptyReportSaysThereAreNoFeedbacks() {
    AdminMessage message = AdminMessageFormatter.weeklyReport(WeeklyReport.of(List.of(today), List.of()));

    assertThat(message.body()).contains("Média das notas: -").contains("(nenhuma avaliação no período)");
  }

  @Test
  void longReportIsTruncated() {
    List<Feedback> many = IntStream.range(0, AdminMessageFormatter.MAX_FEEDBACKS_IN_REPORT + 5)
        .mapToObj(i -> feedback(String.valueOf(i), 8, "2026-11-08"))
        .toList();

    AdminMessage message = AdminMessageFormatter.weeklyReport(WeeklyReport.of(List.of(today), many));

    assertThat(message.body()).contains("... e mais 5 avaliações (lista truncada).");
  }
}
