package com.example.transaction_service_fraud_detection.service;

import com.example.transaction_service_fraud_detection.domain.OutboxEvent;
import com.example.transaction_service_fraud_detection.domain.Transaction;
import com.example.transaction_service_fraud_detection.dto.TransactionRequest;
import com.example.transaction_service_fraud_detection.event.TransactionReceivedEvent;
import com.example.transaction_service_fraud_detection.repository.OutboxRepository;
import com.example.transaction_service_fraud_detection.repository.TransactionRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
public class TransactionalWorker {

    private final TransactionRepository txRepo;
    private final OutboxRepository outboxRepo;
    private final ObjectMapper mapper;

    public TransactionalWorker(TransactionRepository txRepo, OutboxRepository outboxRepo, ObjectMapper mapper) {
        this.txRepo = txRepo;
        this.outboxRepo = outboxRepo;
        this.mapper = mapper;
    }

    @Transactional
    public boolean processInternal(
            TransactionRequest request,
            String idempotencyKey) {

        // Persist transaction
        var tx = new Transaction(request, idempotencyKey);
        txRepo.save(tx);

        // Create event
        var event = TransactionReceivedEvent.from(
                request.transactionId(),
                request.customerId(),
                request.amount(),
                request.currency(),
                request.country(),
                request.deviceId(),
                request.timestamp()
        );

        var payload = mapper.writeValueAsString(event);

        // Persist outbox
        outboxRepo.save(
                new OutboxEvent(
                        request.transactionId(),
                        "TransactionReceived",
                        payload,
                        MDC.get("X-Correlation-Id")
                )
        );

        return true;
    }
}
