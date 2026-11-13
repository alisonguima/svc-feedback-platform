package com.techchallenge.feedback.adapter.output.dynamodb.mapper;

import com.techchallenge.feedback.adapter.output.dynamodb.model.FeedbackItem;
import com.techchallenge.feedback.application.domain.Feedback;
import org.junit.jupiter.api.Test;

import static com.techchallenge.feedback.support.FeedbackFixture.critical;
import static org.assertj.core.api.Assertions.assertThat;

class FeedbackItemMapperTest {

  @Test
  void mapsDomainToItemUsingApiAttributeNames() {
    FeedbackItem item = FeedbackItemMapper.toItem(critical());

    assertThat(item.getId()).isEqualTo("abc-123");
    assertThat(item.getDescricao()).isEqualTo("Aula confusa");
    assertThat(item.getNota()).isEqualTo(2);
    assertThat(item.getUrgencia()).isEqualTo("CRITICAL");
    assertThat(item.getDataEnvio()).isEqualTo("2026-11-09T15:30:00Z");
    assertThat(item.getDia()).isEqualTo("2026-11-09");
  }

  @Test
  void roundTripKeepsAllFields() {
    Feedback original = critical();

    Feedback restored = FeedbackItemMapper.toDomain(FeedbackItemMapper.toItem(original));

    assertThat(restored).usingRecursiveComparison().isEqualTo(original);
  }
}
