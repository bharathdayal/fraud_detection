package com.example.transaction_service_fraud_detection.service;

import com.example.transaction_service_fraud_detection.controller.TransactionController;
import com.example.transaction_service_fraud_detection.domain.OutboxEvent;
import com.example.transaction_service_fraud_detection.domain.Transaction;
import com.example.transaction_service_fraud_detection.dto.TransactionRequest;
import com.example.transaction_service_fraud_detection.event.TransactionReceivedEvent;
import com.example.transaction_service_fraud_detection.repository.OutboxRepository;
import com.example.transaction_service_fraud_detection.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
public class TransactionService {

    private static final Logger log =
            LoggerFactory.getLogger(TransactionService.class);


    private final TransactionalWorker worker;

    public TransactionService(TransactionalWorker worker) {
        this.worker = worker;
    }


    public boolean process(
            TransactionRequest request,
            String idempotencyKey) {

        try {
            return worker.processInternal(request, idempotencyKey);
        } catch (DataIntegrityViolationException ex) {

            log.debug(
                    "Duplicate transaction detected: transactionId={}, idempotencyKey={}",
                    request.transactionId(),
                    idempotencyKey
            );

            return false;
        }
    }



}
