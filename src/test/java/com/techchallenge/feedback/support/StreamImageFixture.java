package com.techchallenge.feedback.support;

import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;

import java.util.Map;

public final class StreamImageFixture {

  private StreamImageFixture() {}

  /** NewImage de uma avaliação crítica, como chega no stream do DynamoDB. */
  public static Map<String, AttributeValue> criticalImage() {
    return Map.of(
        "id", new AttributeValue().withS("abc-123"),
        "descricao", new AttributeValue().withS("Aula confusa"),
        "nota", new AttributeValue().withN("2"),
        "urgencia", new AttributeValue().withS("CRITICAL"),
        "dataEnvio", new AttributeValue().withS("2026-11-09T15:30:00Z"),
        "dia", new AttributeValue().withS("2026-11-09"));
  }
}
