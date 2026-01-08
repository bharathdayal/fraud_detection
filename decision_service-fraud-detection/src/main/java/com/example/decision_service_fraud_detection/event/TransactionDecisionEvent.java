package com.example.decision_service_fraud_detection.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TransactionDecisionEvent(

        @JsonProperty("eventType")
        String eventType,

        @JsonProperty("eventVersion")
        int eventVersion,

        @JsonProperty("transactionId")
        String transactionId,

        @JsonProperty("decision")
        String decision,     // ALLOW / BLOCK / REVIEW

        @JsonProperty("riskScore")
        int riskScore,

        @JsonProperty("riskLevel")
        String riskLevel,

        @JsonProperty("reasons")
        List<String> reasons,

        @JsonProperty("decidedAt")
        Instant decidedAt

) {
    public static TransactionDecisionEvent from(
            String txnId,
            String decision,
            int score,
            String level,
            List<String> reasons) {

        return new TransactionDecisionEvent(
                "TransactionDecision",
                1,
                txnId,
                decision,
                score,
                level,
                reasons,
                Instant.now()
        );
    }
}
