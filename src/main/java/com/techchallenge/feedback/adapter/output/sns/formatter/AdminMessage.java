package com.techchallenge.feedback.adapter.output.sns.formatter;

/** E-mail pronto para publicação no SNS (assunto ASCII, até 100 caracteres). */
public record AdminMessage(String subject, String body) {}
