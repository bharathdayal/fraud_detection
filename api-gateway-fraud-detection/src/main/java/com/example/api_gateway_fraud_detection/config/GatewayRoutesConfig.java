package com.example.api_gateway_fraud_detection.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration

public class GatewayRoutesConfig {

    @Bean
    RouteLocator gatewayRoutes(RouteLocatorBuilder builder, RedisRateLimiter redisRateLimiter,KeyResolver ipKeyResolver){
        return builder.routes()


        // AUTH SERVICE ROUTES
        // =========================

        // OAuth2 Token Endpoint
            .route("auth-token", r -> r
                .path("/oauth2/token")
                .filters(  f->f
                                .circuitBreaker(c->c
                                        .setName("auth-token")
                                        .setFallbackUri("forward:/fallback/auth")

                                )
                        // Strongly recommended: rate limit token calls
                        .requestRateLimiter(c -> c
                                .setRateLimiter(redisRateLimiter)
                                .setKeyResolver(ipKeyResolver)
                        )
                )
                .uri("http://localhost:9001")
             )

                // =========================
                // TRANSACTION INGESTION
                // =========================
                .route("transaction-ingestion", r -> r
                        .path("/api/transactions/**")
                        .filters(f -> f
                                .filter(new CorrelationIdFilter())
                                .requestRateLimiter(c -> c
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(ipKeyResolver)
                                )
                        )
                        .uri("http://localhost:9002")
                )



                .build();

    }


}
