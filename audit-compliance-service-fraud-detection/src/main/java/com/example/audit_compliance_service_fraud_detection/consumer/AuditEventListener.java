package com.example.audit_compliance_service_fraud_detection.consumer;

import com.example.audit_compliance_service_fraud_detection.entity.AuditLedgerEntry;
import com.example.audit_compliance_service_fraud_detection.repository.AuditLedgerRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

@Component
public class AuditEventListener {

    private static final String CORRELATION_ID = "X-Correlation-Id";

    private final ObjectMapper objectMapper;
    private final AuditLedgerRepository repository;

    public AuditEventListener(
            ObjectMapper objectMapper,
            AuditLedgerRepository repository) {
        this.objectMapper = objectMapper;
        this.repository = repository;
    }

    @KafkaListener(
            topics = {
                    "transactions.decisioned",
                    "transactions.actioned"
            },
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(
            ConsumerRecord<String, String> record,
            Acknowledgment ack) {

        try {
            var header = record.headers().lastHeader(CORRELATION_ID);
            if (header != null) {
                MDC.put(
                        CORRELATION_ID,
                        new String(header.value(), StandardCharsets.UTF_8)
                );
            }

            var json = objectMapper.readTree(record.value());

            AuditLedgerEntry entry =
                    new AuditLedgerEntry(
                            json.get("transactionId").asText(),
                            json.get("decision").asText(),
                            json.has("action") ? json.get("action").asText() : null,
                            json.has("riskScore") ? json.get("riskScore").asInt() : 0,
                            json.has("riskLevel") ? json.get("riskLevel").asText() : "NA",
                            json.has("reasons")
                                    ? objectMapper.writeValueAsString(json.get("reasons"))
                                    : null,
                            MDC.get(CORRELATION_ID),
                            json.get("eventType").asText(),
                            json.get("eventVersion").asInt()
                    );

            repository.save(entry);

            ack.acknowledge();

        } catch (Exception ex) {
            // No ACK → retry
        } finally {
            MDC.clear();
        }
    }
}
