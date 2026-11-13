package com.techchallenge.feedback.adapter.output.sns.formatter;

// assunto sem acento e ate 100 chars (limite do SNS)
public record AdminMessage(String subject, String body) {}
