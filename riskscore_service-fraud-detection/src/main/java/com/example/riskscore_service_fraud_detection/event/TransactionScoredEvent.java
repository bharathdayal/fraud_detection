package com.example.riskscore_service_fraud_detection.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TransactionScoredEvent(

        @JsonProperty("eventType")
        String eventType,

        @JsonProperty("eventVersion")
        int eventVersion,

        @JsonProperty("transactionId")
        String transactionId,

        @JsonProperty("riskScore")
        int riskScore,

        @JsonProperty("riskLevel")
        String riskLevel,

        @JsonProperty("reasons")
        List<String> reasons,

        @JsonProperty("signals")
        Map<String, Object> signals

) {
    public static TransactionScoredEvent from(
            String transactionId,
            int score,
            String level,
            List<String> reasons,
            Map<String, Object> signals) {

        return new TransactionScoredEvent(
                "TransactionScored",
                1,
                transactionId,
                score,
                level,
                reasons,
                signals
        );
    }
}