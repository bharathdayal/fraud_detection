package com.example.transaction_service_fraud_detection.event;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionReceivedEvent(

        // ===== Envelope =====
        String eventId,
        String eventType,
        int eventVersion,
        Instant eventTime,

        // ===== Business Identity =====
        String transactionId,
        String customerId,

        // ===== Transaction Data =====
        BigDecimal amount,
        String currency,
        String country,
        String deviceId,
        Instant transactionTime
) {
    public static TransactionReceivedEvent from(
            String transactionId,
            String customerId,
            BigDecimal amount,
            String currency,
            String country,
            String deviceId,
            Instant transactionTime) {

        return new TransactionReceivedEvent(
                java.util.UUID.randomUUID().toString(),
                "TransactionReceived",
                1,
                Instant.now(),
                transactionId,
                customerId,
                amount,
                currency,
                country,
                deviceId,
                transactionTime
        );
    }
}
