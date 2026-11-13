package com.techchallenge.feedback.adapter.input.lambda.dto;

// formato Problem Details (RFC 9457)
public record ProblemResponse(int status, String title, String detail, String timestamp) {}
