package com.techchallenge.feedback.adapter.output.sns.formatter;

import com.techchallenge.feedback.application.domain.Feedback;
import com.techchallenge.feedback.application.domain.WeeklyReport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AdminMessageFormatter {

  static final int MAX_FEEDBACKS_IN_REPORT = 200;

  private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
  private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final DateTimeFormatter DATE_TIME =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(Feedback.BRAZIL_ZONE);

  private AdminMessageFormatter() {}

  public static AdminMessage urgency(Feedback feedback) {
    String subject = "[URGENTE] Feedback critico recebido - nota %d".formatted(feedback.getScore());
    String body = String.join("\n",
        "Uma avaliação CRÍTICA acabou de ser registrada.",
        "",
        "Descrição : " + feedback.getDescription(),
        "Urgência  : " + feedback.getUrgency().getLabel(),
        "Nota      : " + feedback.getScore(),
        "Enviada em: " + DATE_TIME.format(feedback.getSentAt()) + " (horário de Brasília)",
        "ID        : " + feedback.getId());
    return new AdminMessage(subject, body);
  }

  public static AdminMessage weeklyReport(WeeklyReport report) {
    String period = "%s a %s".formatted(DAY.format(report.getStartDay()), DAY.format(report.getEndDay()));
    String subject = "Relatorio semanal de feedbacks - " + period;

    List<String> lines = new ArrayList<>(List.of(
        "RELATÓRIO SEMANAL DE AVALIAÇÕES",
        "Período: " + period,
        "",
        "Total de avaliações: " + report.getTotal(),
        "Média das notas: " + report.getAverageScore().map(AdminMessageFormatter::formatAverage).orElse("-"),
        "",
        "Avaliações por urgência:"));
    report.getCountByUrgency().forEach((urgency, count) -> lines.add("  - %s: %d".formatted(urgency.getLabel(), count)));

    lines.add("");
    lines.add("Avaliações por dia:");
    report.getCountByDay().forEach((day, count) -> lines.add("  - %s: %d".formatted(formatDay(day), count)));

    lines.add("");
    lines.add("Lista de avaliações (descrição | urgência | nota | data de envio):");
    if (report.getFeedbacks().isEmpty()) {
      lines.add("  (nenhuma avaliação no período)");
    }
    report.getFeedbacks().stream()
        .limit(MAX_FEEDBACKS_IN_REPORT)
        .forEach(f -> lines.add("  - %s | %s | %d | %s".formatted(
            f.getDescription(), f.getUrgency().getLabel(), f.getScore(), DATE_TIME.format(f.getSentAt()))));
    if (report.getFeedbacks().size() > MAX_FEEDBACKS_IN_REPORT) {
      lines.add("  ... e mais %d avaliações (lista truncada)."
          .formatted(report.getFeedbacks().size() - MAX_FEEDBACKS_IN_REPORT));
    }

    return new AdminMessage(subject, String.join("\n", lines));
  }

  private static String formatAverage(BigDecimal average) {
    return String.format(PT_BR, "%.2f", average);
  }

  private static String formatDay(LocalDate day) {
    return DAY.format(day);
  }
}
