package com.example.transaction_service_fraud_detection.service;

import com.example.transaction_service_fraud_detection.domain.OutboxEvent;
import com.example.transaction_service_fraud_detection.repository.OutboxRepository;
import jakarta.transaction.Transactional;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class OutboxPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxPublisher.class);

    private static final String TOPIC = "transactions.raw";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(
            OutboxRepository outboxRepository,
            KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void publishPendingEvents() {

        var events =
                outboxRepository.findTop50ByStatusOrderByCreatedAt(
                        OutboxEvent.Status.PENDING
                );

        for (var event : events) {
            try {
                // Rehydrate MDC for logging
                if (event.getCorrelationId() != null) {
                    MDC.put("X-Correlation-Id", event.getCorrelationId());
                }

                var record = buildProducerRecord(event);
                kafkaTemplate.send(record).get();

                event.markPublished();

                log.debug(
                        "Publishing outbox event id={} with correlationId={}",
                        event.getId(),
                        event.getCorrelationId()
                );

            } catch (Exception ex) {

                log.error(
                        "Failed to publish outbox event id={}, retryCount={}",
                        event.getId(),
                        event.getRetryCount(),
                        ex
                );

                if (event.canRetry(5)) {
                    event.markRetry();
                } else {
                    event.markFailed();
                }

            } finally {
                MDC.clear(); // CRITICAL
            }
        }
    }

    private ProducerRecord<String, String> buildProducerRecord(
            OutboxEvent event) {

        var record = new ProducerRecord<>(
                TOPIC,
                event.getAggregateId(),
                event.getPayload()
        );

        if (event.getCorrelationId() != null) {
            record.headers().add(
                    "X-Correlation-Id",
                    event.getCorrelationId().getBytes(StandardCharsets.UTF_8)
            );
        }

        return record;
    }
}
