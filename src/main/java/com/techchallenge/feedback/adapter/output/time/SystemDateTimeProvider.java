package com.techchallenge.feedback.adapter.output.time;

import com.techchallenge.feedback.application.port.output.DateTimeProviderPort;

import java.time.Instant;

public class SystemDateTimeProvider implements DateTimeProviderPort {

  @Override
  public Instant now() {
    return Instant.now();
  }
}
