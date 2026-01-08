package com.example.alert_case_service_fraud_detection.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name="FraudCase")
@Data
@AllArgsConstructor
public class FraudCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String transactionId;

    @Column(nullable=false)
    private String decision;

    @Column(nullable=false)
    private String priority;

    @Column(nullable=false)
    private String status;

    private Integer riskScore;
    private String riskLevel;

    @Column(columnDefinition="json")
    private String reasons;

    private String correlationId;

    @Column(nullable=false, updatable=false)
    private Instant openedAt = Instant.now();

    @Column(nullable=false)
    private Instant updatedAt = Instant.now();

    protected FraudCase() {}

    public FraudCase(String txnId, String decision, String priority,
                     Integer riskScore, String riskLevel, String reasons, String correlationId) {
        this.transactionId = txnId;
        this.decision = decision;
        this.priority = priority;
        this.status = "OPEN";
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.reasons = reasons;
        this.correlationId = correlationId;
    }

    public void markInProgress() {
        this.status = "IN_PROGRESS";
        this.updatedAt = Instant.now();
    }

    public void close() {
        this.status = "CLOSED";
        this.updatedAt = Instant.now();
    }
}
