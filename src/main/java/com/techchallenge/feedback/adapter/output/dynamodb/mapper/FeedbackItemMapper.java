package com.techchallenge.feedback.adapter.output.dynamodb.mapper;

import com.techchallenge.feedback.adapter.output.dynamodb.model.FeedbackItem;
import com.techchallenge.feedback.application.domain.Feedback;
import com.techchallenge.feedback.application.domain.enums.UrgencyEnum;

import java.time.Instant;
import java.time.LocalDate;

// sem mapstruct, nao compensa pra 2 metodos
public final class FeedbackItemMapper {

  private FeedbackItemMapper() {}

  public static FeedbackItem toItem(Feedback feedback) {
    return new FeedbackItem(
        feedback.getId(),
        feedback.getDescription(),
        feedback.getScore(),
        feedback.getUrgency().name(),
        feedback.getSentAt().toString(),
        feedback.getSentDay().toString());
  }

  public static Feedback toDomain(FeedbackItem item) {
    return new Feedback(
        item.getId(),
        item.getDescricao(),
        item.getNota(),
        UrgencyEnum.valueOf(item.getUrgencia()),
        Instant.parse(item.getDataEnvio()),
        LocalDate.parse(item.getDia()));
  }
}
