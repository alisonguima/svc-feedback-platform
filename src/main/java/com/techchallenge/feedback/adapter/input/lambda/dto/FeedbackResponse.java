package com.techchallenge.feedback.adapter.input.lambda.dto;

/** Resposta do {@code POST /avaliacao} (201 Created). */
public record FeedbackResponse(String id, String urgencia, String dataEnvio) {}
