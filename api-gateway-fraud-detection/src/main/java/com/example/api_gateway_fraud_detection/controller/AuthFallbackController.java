package com.example.api_gateway_fraud_detection.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("fallback/auth")
public class AuthFallbackController {

    @PostMapping
    public Mono<ResponseEntity<Map<String,String>>> authFallBack() {
        return Mono.just(
                ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of(
                                "error", "AUTH_SERVICE_UNAVAILABLE",
                                "message", "Authentication service is temporarily unavailable. Please try again later."
                        ))
        );
    }


    @GetMapping
    public Mono<ResponseEntity<Map<String, String>>> jwksFallback() {
        return Mono.just(
                ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of(
                                "error", "JWKS_UNAVAILABLE",
                                "message", "JWKS endpoint is temporarily unavailable."
                        ))
        );
    }
}
