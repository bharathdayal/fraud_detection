package com.example.alert_case_service_fraud_detection.consumer;

import com.example.alert_case_service_fraud_detection.service.CaseService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

@Component
public class DecisionListener {

    private static final String CID = "X-Correlation-Id";

    private final ObjectMapper mapper;
    private final CaseService caseService;

    public DecisionListener(ObjectMapper mapper, CaseService caseService) {
        this.mapper = mapper;
        this.caseService = caseService;
    }

    @KafkaListener(
            topics = "transactions.decisioned",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String,String> record, Acknowledgment ack) {
        try {
            var h = record.headers().lastHeader(CID);
            if (h != null) MDC.put(CID, new String(h.value(), StandardCharsets.UTF_8));

            var json = mapper.readTree(record.value());

            caseService.createCaseIfRequired(
                    json.get("transactionId").asText(),
                    json.get("decision").asText(),
                    json.get("riskScore").asInt(),
                    json.get("riskLevel").asText(),
                    mapper.writeValueAsString(json.get("reasons")),
                    MDC.get(CID)
            );

            ack.acknowledge();
        } catch (Exception e) {
            // retry
        } finally {
            MDC.clear();
        }
    }
}
