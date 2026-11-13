package com.techchallenge.feedback.adapter.input.lambda.dto;

/** Erro no formato Problem Details (RFC 9457), o mesmo padrão dos serviços Spring. */
public record ProblemResponse(int status, String title, String detail, String timestamp) {}
