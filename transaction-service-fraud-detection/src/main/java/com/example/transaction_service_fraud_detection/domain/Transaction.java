package com.example.transaction_service_fraud_detection.domain;

import com.example.transaction_service_fraud_detection.dto.TransactionRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "transactions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_transaction_id",
                        columnNames = "transaction_id"
                ),
                @UniqueConstraint(
                        name = "uk_idempotency_key",
                        columnNames = "idempotency_key"
                )
        }
)
@Data
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    private String customerId;
    private BigDecimal amount;
    private String currency;
    private String country;
    private String deviceId;
    private Instant createdAt;

    protected Transaction() {}

    public Transaction(TransactionRequest req,String idempotencyKey) {
        this.transactionId = req.transactionId();
        this.customerId = req.customerId();
        this.amount = req.amount();
        this.currency = req.currency();
        this.country = req.country();
        this.deviceId = req.deviceId();
        this.createdAt = Instant.now();
        this.idempotencyKey=idempotencyKey;
    }
}
