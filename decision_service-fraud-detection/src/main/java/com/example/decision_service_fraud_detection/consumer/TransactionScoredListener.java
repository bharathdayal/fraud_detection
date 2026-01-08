package com.example.decision_service_fraud_detection.consumer;

import com.example.decision_service_fraud_detection.event.TransactionDecisionEvent;
import com.example.decision_service_fraud_detection.service.DecisionPolicyService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class TransactionScoredListener {

    private static final String CORRELATION_ID = "X-Correlation-Id";
    private static final String DECISION_TOPIC = "transactions.decisioned";

    private final ObjectMapper objectMapper;
    private final DecisionPolicyService policyService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public TransactionScoredListener(
            ObjectMapper objectMapper,
            DecisionPolicyService policyService,
            KafkaTemplate<String, String> kafkaTemplate) {

        this.objectMapper = objectMapper;
        this.policyService = policyService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(
            topics = "transactions.scored",
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

            String txnId = json.get("transactionId").asText();
            int score = json.get("riskScore").asInt();
            String level = json.get("riskLevel").asText();

            List<String> reasons =
                    objectMapper.convertValue(
                            json.get("reasons"),
                            List.class
                    );

            String decision =
                    policyService.decide(level, score);

            var decisionEvent =
                    TransactionDecisionEvent.from(
                            txnId,
                            decision,
                            score,
                            level,
                            reasons
                    );

            String payload =
                    objectMapper.writeValueAsString(decisionEvent);

            ProducerRecord<String, String> out =
                    new ProducerRecord<>(
                            DECISION_TOPIC,
                            txnId,
                            payload
                    );

            if (header != null) out.headers().add(header);

            kafkaTemplate.send(out).get();

            ack.acknowledge();

        } catch (Exception ex) {
            // No ACK → retry
        } finally {
            MDC.clear();
        }
    }

}
