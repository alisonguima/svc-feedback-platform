package com.techchallenge.feedback.adapter.input.lambda.mapper;

import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.techchallenge.feedback.application.domain.Feedback;
import com.techchallenge.feedback.application.domain.enums.UrgencyEnum;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

public final class FeedbackStreamMapper {

  private FeedbackStreamMapper() {}

  public static Feedback toDomain(Map<String, AttributeValue> image) {
    return new Feedback(
        image.get("id").getS(),
        image.get("descricao").getS(),
        Integer.valueOf(image.get("nota").getN()),
        UrgencyEnum.valueOf(image.get("urgencia").getS()),
        Instant.parse(image.get("dataEnvio").getS()),
        LocalDate.parse(image.get("dia").getS()));
  }
}
