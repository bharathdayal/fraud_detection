package com.example.alert_case_service_fraud_detection.service;

import com.example.alert_case_service_fraud_detection.entity.FraudCase;
import com.example.alert_case_service_fraud_detection.repository.FraudCaseRepository;
import org.springframework.stereotype.Service;

@Service
public class CaseService {

    private final FraudCaseRepository repo;

    public CaseService(FraudCaseRepository repo) {
        this.repo = repo;
    }

    public void createCaseIfRequired(String txnId, String decision,
                                     Integer score, String level,
                                     String reasons, String correlationId) {

        if ("REVIEW".equals(decision) || "BLOCK".equals(decision)) {

            String priority = "BLOCK".equals(decision) ? "HIGH" : "MEDIUM";

            repo.findByTransactionId(txnId)
                    .orElseGet(() -> repo.save(
                            new FraudCase(
                                    txnId,
                                    decision,
                                    priority,
                                    score,
                                    level,
                                    reasons,
                                    correlationId
                            )
                    ));
        }
    }
}
