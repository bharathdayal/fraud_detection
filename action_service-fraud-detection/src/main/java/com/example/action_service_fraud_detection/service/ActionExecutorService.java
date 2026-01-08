package com.example.action_service_fraud_detection.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ActionExecutorService {

    private static final Logger log =
            LoggerFactory.getLogger(ActionExecutorService.class);

    public String execute(String transactionId, String decision) {

        return switch (decision) {

            case "ALLOW" -> {
                log.info("Transaction {} approved", transactionId);
                yield "APPROVED";
            }

            case "REVIEW" -> {
                log.info("Transaction {} sent for manual review", transactionId);
                yield "SENT_FOR_REVIEW";
            }

            case "BLOCK" -> {
                log.warn("Transaction {} blocked due to fraud risk", transactionId);
                yield "REJECTED";
            }

            default -> {
                log.error("Unknown decision {} for txn {}", decision, transactionId);
                yield "UNKNOWN";
            }
        };
    }
}
