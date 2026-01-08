package com.example.transaction_service_fraud_detection.config;


import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String,String>producerFactory(){
        Map<String, Object> props = new HashMap<>();

        // ===============================
        // Broker
        // ===============================
        props.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9092"
        );

        // ===============================
        // Serialization
        // ===============================
        props.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );
        props.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        // ===============================
        // Reliability (PRODUCTION-GRADE)
        // ===============================
        props.put(
                ProducerConfig.ACKS_CONFIG,
                "all"
        );
        props.put(
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,
                true
        );
        props.put(
                ProducerConfig.RETRIES_CONFIG,
                Integer.MAX_VALUE
        );
        props.put(
                ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION,
                5
        );

        // ===============================
        // Performance / Safety
        // ===============================
        props.put(
                ProducerConfig.LINGER_MS_CONFIG,
                5
        );
        props.put(
                ProducerConfig.BATCH_SIZE_CONFIG,
                32_768
        );

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String,String> kafkaTemplate(
            ProducerFactory<String,String>producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }
}
