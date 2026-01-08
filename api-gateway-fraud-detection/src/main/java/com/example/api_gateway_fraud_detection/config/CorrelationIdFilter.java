package com.example.api_gateway_fraud_detection.config;


import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdFilter implements GatewayFilter {

    private static final String CORRELATION_ID = "X-Correlation-Id";


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var headers = exchange.getRequest().getHeaders();
        var correlationId =
                headers.getFirst(CORRELATION_ID) != null
                            ? headers.getFirst(CORRELATION_ID)
                            : UUID.randomUUID().toString();

        var mutatedRequest =  exchange.getRequest()
                .mutate()
                .header(CORRELATION_ID,correlationId)
                .build();


        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }
}
