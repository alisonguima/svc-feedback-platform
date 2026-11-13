package com.techchallenge.feedback.adapter.input.lambda.handler;

/** Valores mínimos para os construtores padrão montarem os clientes da AWS (sem chamadas de rede). */
final class HandlerEnvironment {

  private HandlerEnvironment() {}

  static void configure() {
    System.setProperty("aws.region", "us-east-1");
    System.setProperty("TABLE_NAME", "avaliacoes-teste");
    System.setProperty("ADMIN_TOPIC_ARN", "arn:aws:sns:us-east-1:123456789012:admin");
  }
}
