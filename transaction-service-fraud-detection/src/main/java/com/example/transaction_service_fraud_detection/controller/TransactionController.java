package com.example.transaction_service_fraud_detection.controller;

import com.example.transaction_service_fraud_detection.dto.TransactionRequest;
import com.example.transaction_service_fraud_detection.service.OutboxPublisher;
import com.example.transaction_service_fraud_detection.service.TransactionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private static final Logger log =
            LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<?> submit(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransactionRequest request
            ) {

        var accepted = transactionService.process(request,idempotencyKey);
        log.debug("Received transaction request {}", request.transactionId());

        return  accepted
                    ? ResponseEntity.ok(Map.of("status", "RECEIVED"))
                    : ResponseEntity.status(409)
                .body(Map.of("status", "DUPLICATE"));

    }
}
