package com.example.action_service_fraud_detection.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TransactionActionEvent(

        @JsonProperty("eventType")
        String eventType,

        @JsonProperty("eventVersion")
        int eventVersion,

        @JsonProperty("transactionId")
        String transactionId,

        @JsonProperty("decision")
        String decision,   // ALLOW / BLOCK / REVIEW

        @JsonProperty("action")
        String action,     // APPROVED / REJECTED / SENT_FOR_REVIEW

        @JsonProperty("actionedAt")
        Instant actionedAt

) {
    public static TransactionActionEvent from(
            String txnId,
            String decision,
            String action) {

        return new TransactionActionEvent(
                "TransactionActioned",
                1,
                txnId,
                decision,
                action,
                Instant.now()
        );
    }
}
