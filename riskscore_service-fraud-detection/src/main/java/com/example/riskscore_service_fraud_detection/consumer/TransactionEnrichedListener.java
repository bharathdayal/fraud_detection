package com.example.riskscore_service_fraud_detection.consumer;

import com.example.riskscore_service_fraud_detection.event.TransactionScoredEvent;
import com.example.riskscore_service_fraud_detection.service.RiskRuleEngine;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class TransactionEnrichedListener {

    private static final Logger log =
            LoggerFactory.getLogger(TransactionEnrichedListener.class);

    private static final String CORRELATION_ID = "X-Correlation-Id";
    private static final String SCORED_TOPIC = "transactions.scored";

    private final ObjectMapper objectMapper;
    private final RiskRuleEngine ruleEngine;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public TransactionEnrichedListener(ObjectMapper objectMapper, RiskRuleEngine ruleEngine, KafkaTemplate<String, String> kafkaTemplate) {
        this.objectMapper = objectMapper;
        this.ruleEngine = ruleEngine;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(
            topics = "transactions.enriched",
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

            var enriched =
                    objectMapper.readTree(record.value());

            String txnId =
                    enriched.get("transactionId").asText();

            Map<String, Object> signals =
                    objectMapper.convertValue(
                            enriched.get("signals"),
                            Map.class
                    );

            var result = ruleEngine.evaluate(signals);

            String riskLevel =
                    result.score() >= 70 ? "HIGH" :
                            result.score() >= 30 ? "MEDIUM" : "LOW";

            var scoredEvent =
                    TransactionScoredEvent.from(
                            txnId,
                            result.score(),
                            riskLevel,
                            result.reasons(),
                            signals
                    );

            String payload =
                    objectMapper.writeValueAsString(scoredEvent);

            ProducerRecord<String, String> out =
                    new ProducerRecord<>(
                            SCORED_TOPIC,
                            txnId,
                            payload
                    );

            if (header != null) out.headers().add(header);

            kafkaTemplate.send(out).get();

            log.debug(
                    "Scored txn={} score={} level={}",
                    txnId,
                    result.score(),
                    riskLevel
            );

            ack.acknowledge();

        } catch (Exception ex) {
            log.error("Risk scoring failed", ex);
        } finally {
            MDC.clear();
        }
    }
}
