package com.example.alert_case_service_fraud_detection.repository;

import com.example.alert_case_service_fraud_detection.entity.FraudCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FraudCaseRepository  extends JpaRepository<FraudCase,Long> {

    Optional<FraudCase> findByTransactionId(String transactionId);
}
