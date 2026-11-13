package com.techchallenge.feedback.adapter.input.lambda.dto;

/** Corpo do {@code POST /avaliacao}, conforme o contrato do enunciado. */
public record FeedbackRequest(String descricao, Integer nota) {}
