package com.techchallenge.feedback.adapter.input.lambda.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.techchallenge.feedback.application.domain.WeeklyReport;
import com.techchallenge.feedback.application.port.input.GenerateWeeklyReportPort;
import com.techchallenge.feedback.config.UseCaseConfig;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

// gerar-relatorio: scheduler (seg 08h). aceita {"dias": N} pra testar na mao
public class WeeklyReportHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

  private static final String DAYS_FIELD = "dias";

  private final GenerateWeeklyReportPort generateWeeklyReportPort;

  public WeeklyReportHandler() {
    this(UseCaseConfig.generateWeeklyReportPort());
  }

  WeeklyReportHandler(GenerateWeeklyReportPort generateWeeklyReportPort) {
    this.generateWeeklyReportPort = generateWeeklyReportPort;
  }

  @Override
  public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
    int days = Optional.ofNullable(input)
        .map(map -> map.get(DAYS_FIELD))
        .filter(Number.class::isInstance)
        .map(value -> ((Number) value).intValue())
        .orElse(WeeklyReport.DEFAULT_PERIOD_DAYS);

    WeeklyReport report = generateWeeklyReportPort.generate(days);

    Map<String, Object> summary = new LinkedHashMap<>();
    summary.put("inicio", report.getStartDay().toString());
    summary.put("fim", report.getEndDay().toString());
    summary.put("total", report.getTotal());
    summary.put("media", report.getAverageScore().map(Object::toString).orElse(null));
    summary.put("porUrgencia", report.getCountByUrgency().toString());
    summary.put("porDia", report.getCountByDay().toString());
    return summary;
  }
}
