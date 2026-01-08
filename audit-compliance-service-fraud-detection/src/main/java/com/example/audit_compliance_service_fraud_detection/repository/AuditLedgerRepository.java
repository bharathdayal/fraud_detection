package com.example.audit_compliance_service_fraud_detection.repository;

import com.example.audit_compliance_service_fraud_detection.entity.AuditLedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLedgerRepository extends JpaRepository<AuditLedgerEntry,Long> {
}
