package com.example.riskscore_service_fraud_detection.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RiskRuleEngine {

    public Result evaluate(Map<String, Object> signals) {

        int score = 0;
        List<String> reasons = new ArrayList<>();

        score += rule(signals, "customer_velocity_1m", 5, 20,
                "High velocity in last 1 minute", reasons);

        score += rule(signals, "customer_velocity_5m", 10, 15,
                "Burst activity in last 5 minutes", reasons);

        score += booleanRule(signals, "geo_changed", 25,
                "Country changed since last transaction", reasons);

        score += rule(signals, "device_reuse_count", 3, 20,
                "Device used by multiple customers", reasons);

        score += rule(signals, "customer_device_count_24h", 4, 10,
                "Customer using many devices", reasons);

        return new Result(score, reasons);
    }

    private int rule(
            Map<String, Object> signals,
            String key,
            int threshold,
            int weight,
            String reason,
            List<String> reasons) {

        Object value = signals.get(key);
        if (value instanceof Number n && n.intValue() > threshold) {
            reasons.add(reason);
            return weight;
        }
        return 0;
    }

    private int booleanRule(
            Map<String, Object> signals,
            String key,
            int weight,
            String reason,
            List<String> reasons) {

        if (Boolean.TRUE.equals(signals.get(key))) {
            reasons.add(reason);
            return weight;
        }
        return 0;
    }

    public record Result(int score, List<String> reasons) {}
}
