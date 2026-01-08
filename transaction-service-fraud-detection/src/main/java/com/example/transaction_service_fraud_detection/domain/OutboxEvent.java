package com.example.transaction_service_fraud_detection.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "outbox_events", indexes = {
        @Index(name = "idx_outbox_status", columnList = "status")
})
@Data
@AllArgsConstructor
public class OutboxEvent {

    public enum Status {
        PENDING,
        PUBLISHED,
        FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String aggregateId;
    private String eventType;

    @Lob
    private String payload;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    private Instant createdAt = Instant.now();

    private int retryCount = 0;
    private Instant lastAttemptAt;

    @Column(nullable = false)
    private String correlationId;

    protected OutboxEvent() {}

    public OutboxEvent(String aggregateId, String eventType, String payload,String correlationId) {
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.correlationId=correlationId;
    }

    public void markPublished() {
        this.status = Status.PUBLISHED;
    }



    public boolean canRetry(int maxRetries) {
        return retryCount < maxRetries;
    }

    public void markRetry() {
        this.retryCount++;
        this.lastAttemptAt = Instant.now();
        this.status = Status.PENDING;
    }

    public void markFailed() {
        this.status = Status.FAILED;
        this.lastAttemptAt = Instant.now();
    }

}
