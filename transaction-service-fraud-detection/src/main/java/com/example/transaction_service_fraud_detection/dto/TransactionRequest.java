package com.example.transaction_service_fraud_detection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionRequest(
        @NotBlank String transactionId,
        String idempotencyKey,
        @NotBlank String customerId,
        @NotNull BigDecimal amount,
        @NotBlank String currency,
        @NotBlank String country,
        @NotBlank String deviceId,
        @NotNull Instant timestamp

) {
}
