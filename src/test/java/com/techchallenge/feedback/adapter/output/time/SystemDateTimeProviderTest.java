package com.techchallenge.feedback.adapter.output.time;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SystemDateTimeProviderTest {

  @Test
  void returnsCurrentInstant() {
    Instant before = Instant.now();

    assertThat(new SystemDateTimeProvider().now()).isBetween(before, Instant.now());
  }
}
