package com.example.enrichment_service_fraud_detection.event;

import com.example.enrichment_service_fraud_detection.dto.TransactionReceivedEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TransactionEnrichedEvent(

        @JsonProperty("eventType")
        String eventType,

        @JsonProperty("eventVersion")
        int eventVersion,

        @JsonProperty("transactionId")
        String transactionId,

        @JsonProperty("customerId")
        String customerId,

        @JsonProperty("amount")
        BigDecimal amount,

        @JsonProperty("currency")
        String currency,

        @JsonProperty("country")
        String country,

        @JsonProperty("deviceId")
        String deviceId,

        @JsonProperty("timestamp")
        Instant timestamp,

        @JsonProperty("signals")
        Map<String, Object> signals

) {
    public static TransactionEnrichedEvent from(
            TransactionReceivedEvent base,
            Map<String, Object> signals) {

        return new TransactionEnrichedEvent(
                "TransactionEnriched",
                1,
                base.transactionId(),
                base.customerId(),
                base.amount(),
                base.currency(),
                base.country(),
                base.deviceId(),
                base.timestamp(),
                signals
        );
    }
}
