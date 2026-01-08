package com.example.enrichment_service_fraud_detection.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class GeoDeviceSignalService {

    private final StringRedisTemplate redis;

    public GeoDeviceSignalService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public Map<String, Object> evaluate(
            String customerId,
            String country,
            String deviceId) {

        Map<String, Object> signals = new HashMap<>();

        // -------- GEO SIGNALS --------
        String lastCountryKey = "geo:last_country:" + customerId;
        String countriesKey = "geo:countries:" + customerId;

        String lastCountry = redis.opsForValue().get(lastCountryKey);

        boolean geoChanged =
                lastCountry != null && !lastCountry.equals(country);

        redis.opsForValue().set(lastCountryKey, country);
        redis.opsForSet().add(countriesKey, country);

        signals.put("geo_changed", geoChanged);
        signals.put("distinct_countries_count",
                redis.opsForSet().size(countriesKey));

        // -------- DEVICE SIGNALS --------
        String deviceCustomersKey = "device:customers:" + deviceId;
        String customerDevicesKey = "customer:devices:" + customerId;

        redis.opsForSet().add(deviceCustomersKey, customerId);
        redis.opsForSet().add(customerDevicesKey, deviceId);

        // TTL for rolling 24h device usage
        redis.expire(customerDevicesKey, Duration.ofHours(24));

        Long deviceReuseCount =
                redis.opsForSet().size(deviceCustomersKey);

        Long customerDeviceCount =
                redis.opsForSet().size(customerDevicesKey);

        signals.put("device_reuse_count",
                deviceReuseCount != null ? deviceReuseCount : 0);

        signals.put("customer_device_count_24h",
                customerDeviceCount != null ? customerDeviceCount : 0);

        return signals;
    }
}
