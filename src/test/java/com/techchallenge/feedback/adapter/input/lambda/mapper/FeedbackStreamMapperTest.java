package com.techchallenge.feedback.adapter.input.lambda.mapper;

import com.techchallenge.feedback.application.domain.Feedback;
import com.techchallenge.feedback.application.domain.enums.UrgencyEnum;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static com.techchallenge.feedback.support.StreamImageFixture.criticalImage;
import static org.assertj.core.api.Assertions.assertThat;

class FeedbackStreamMapperTest {

  @Test
  void mapsStreamImageToDomain() {
    Feedback feedback = FeedbackStreamMapper.toDomain(criticalImage());

    assertThat(feedback.getId()).isEqualTo("abc-123");
    assertThat(feedback.getDescription()).isEqualTo("Aula confusa");
    assertThat(feedback.getScore()).isEqualTo(2);
    assertThat(feedback.getUrgency()).isEqualTo(UrgencyEnum.CRITICAL);
    assertThat(feedback.getSentAt()).isEqualTo(Instant.parse("2026-11-09T15:30:00Z"));
    assertThat(feedback.getSentDay()).isEqualTo(LocalDate.of(2026, 11, 9));
  }
}
