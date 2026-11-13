package com.techchallenge.feedback.application.port.output;

import java.time.Instant;

public interface DateTimeProviderPort {

  Instant now();
}
