package com.example.transaction_service_fraud_detection.repository;

import com.example.transaction_service_fraud_detection.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction,Long> {

    boolean existsByIdempotencyKey(String idempotencyKey);
}
