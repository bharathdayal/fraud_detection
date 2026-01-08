package com.example.enrichment_service_fraud_detection.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
public class VelocityService {

    private final StringRedisTemplate redis;


    public VelocityService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public Map<String, Long> recordAndGetVelocity(
            String customerId,
            String deviceId) {

        long cust1m = increment("customer", customerId, "1m", Duration.ofMinutes(1));
        long cust5m = increment("customer", customerId, "5m", Duration.ofMinutes(5));
        long cust1h = increment("customer", customerId, "1h", Duration.ofHours(1));

        long dev1m = increment("device", deviceId, "1m", Duration.ofMinutes(1));
        long dev5m = increment("device", deviceId, "5m", Duration.ofMinutes(5));
        long dev1h = increment("device", deviceId, "1h", Duration.ofHours(1));

        return Map.of(
                "customer_velocity_1m", cust1m,
                "customer_velocity_5m", cust5m,
                "customer_velocity_1h", cust1h,
                "device_velocity_1m", dev1m,
                "device_velocity_5m", dev5m,
                "device_velocity_1h", dev1h
        );
    }

    private long increment(
            String entity,
            String id,
            String window,
            Duration ttl) {

        String key = "velocity:%s:%s:%s".formatted(entity, id, window);

        Long value = redis.opsForValue().increment(key);

        // Set TTL only on first creation
        if (value != null && value == 1L) {
            redis.expire(key, ttl);
        }

        return value != null ? value : 0L;
    }
}
