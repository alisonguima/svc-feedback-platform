package com.techchallenge.feedback.application.domain;

import com.techchallenge.feedback.application.domain.enums.UrgencyEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * Consolidado das avaliações de um período: lista das avaliações, média das
 * notas, quantidade por dia e quantidade por urgência.
 */
@Getter
@AllArgsConstructor
public class WeeklyReport {

  public static final int DEFAULT_PERIOD_DAYS = 7;

  private LocalDate startDay;
  private LocalDate endDay;
  private List<Feedback> feedbacks;
  private Map<LocalDate, Long> countByDay;
  private Map<UrgencyEnum, Long> countByUrgency;

  /** Dias do período terminando em {@code endDay} (inclusive), em ordem crescente. */
  public static List<LocalDate> periodEndingAt(LocalDate endDay, int days) {
    if (days < 1) {
      throw new IllegalArgumentException("O período do relatório deve ter pelo menos 1 dia.");
    }
    return IntStream.range(0, days)
        .mapToObj(offset -> endDay.minusDays(days - 1L - offset))
        .toList();
  }

  public static WeeklyReport of(List<LocalDate> period, List<Feedback> feedbacks) {
    List<Feedback> ordered = feedbacks.stream()
        .sorted(Comparator.comparing(Feedback::getSentAt))
        .toList();

    Map<LocalDate, Long> countByDay = new LinkedHashMap<>();
    period.forEach(day -> countByDay.put(day, countWhere(ordered, f -> f.getSentDay().equals(day))));

    Map<UrgencyEnum, Long> countByUrgency = new EnumMap<>(UrgencyEnum.class);
    Arrays.stream(UrgencyEnum.values())
        .forEach(u -> countByUrgency.put(u, countWhere(ordered, f -> f.getUrgency() == u)));

    return new WeeklyReport(period.getFirst(), period.getLast(), ordered, countByDay, countByUrgency);
  }

  public long getTotal() {
    return feedbacks.size();
  }

  /** Média das notas com 2 casas decimais; vazio quando não há avaliações no período. */
  public Optional<BigDecimal> getAverageScore() {
    return Optional.of(feedbacks)
        .filter(list -> !list.isEmpty())
        .map(list -> BigDecimal.valueOf(list.stream().mapToInt(Feedback::getScore).sum())
            .divide(BigDecimal.valueOf(list.size()), 2, RoundingMode.HALF_UP));
  }

  private static long countWhere(List<Feedback> feedbacks, Predicate<Feedback> predicate) {
    return feedbacks.stream().filter(predicate).count();
  }
}
