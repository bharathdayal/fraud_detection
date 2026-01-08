package com.example.decision_service_fraud_detection.service;

import org.springframework.stereotype.Service;

@Service
public class DecisionPolicyService {

    public String decide(String riskLevel, int riskScore) {

        return switch (riskLevel) {
            case "HIGH" -> "BLOCK";
            case "MEDIUM" -> "REVIEW";
            default -> "ALLOW";
        };
    }
}
