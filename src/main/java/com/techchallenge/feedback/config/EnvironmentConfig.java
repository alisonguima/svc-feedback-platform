package com.techchallenge.feedback.config;

import java.util.Optional;

/** Lê configuração das variáveis de ambiente do Lambda (com fallback para system properties nos testes). */
public final class EnvironmentConfig {

  public static final String TABLE_NAME = "TABLE_NAME";
  public static final String ADMIN_TOPIC_ARN = "ADMIN_TOPIC_ARN";

  private EnvironmentConfig() {}

  public static String require(String name) {
    return Optional.ofNullable(System.getenv(name))
        .or(() -> Optional.ofNullable(System.getProperty(name)))
        .filter(value -> !value.isBlank())
        .orElseThrow(() -> new IllegalStateException("Variável de ambiente obrigatória ausente: " + name));
  }
}
