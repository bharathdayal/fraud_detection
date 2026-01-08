package com.example.transaction_service_fraud_detection.repository;

import com.example.transaction_service_fraud_detection.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEvent,Long> {

    List<OutboxEvent> findByStatus(OutboxEvent.Status status);
    List<OutboxEvent> findTop50ByStatusOrderByCreatedAt(
            OutboxEvent.Status status
    );
}
