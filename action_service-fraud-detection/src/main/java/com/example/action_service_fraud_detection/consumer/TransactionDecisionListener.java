package com.example.action_service_fraud_detection.consumer;

import com.example.action_service_fraud_detection.event.TransactionActionEvent;
import com.example.action_service_fraud_detection.service.ActionExecutorService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

@Component
public class TransactionDecisionListener {

    private static final String CORRELATION_ID = "X-Correlation-Id";
    private static final String ACTION_TOPIC = "transactions.actioned";

    private final ObjectMapper objectMapper;
    private final ActionExecutorService executor;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public TransactionDecisionListener(
            ObjectMapper objectMapper,
            ActionExecutorService executor,
            KafkaTemplate<String, String> kafkaTemplate) {

        this.objectMapper = objectMapper;
        this.executor = executor;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(
            topics = "transactions.decisioned",
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
            String decision = json.get("decision").asText();

            String action =
                    executor.execute(txnId, decision);

            var actionEvent =
                    TransactionActionEvent.from(
                            txnId,
                            decision,
                            action
                    );

            String payload =
                    objectMapper.writeValueAsString(actionEvent);

            ProducerRecord<String, String> out =
                    new ProducerRecord<>(
                            ACTION_TOPIC,
                            txnId,
                            payload
                    );

            if (header != null) out.headers().add(header);

            kafkaTemplate.send(out).get();

            ack.acknowledge();

        } catch (Exception ex) {
            // No ACK → Kafka retry
        } finally {
            MDC.clear();
        }
    }
}
