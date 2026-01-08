package com.example.api_gateway_fraud_detection.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * Auth endpoints – NO JWT
     */
    @Bean
    @Order(0)
    SecurityWebFilterChain authEndpointsSecurity(ServerHttpSecurity http) {

        return http
                .securityMatcher(
                        ServerWebExchangeMatchers.pathMatchers(
                                "/oauth2/token",
                                "/oauth2/jwks"
                        )
                )
                .authorizeExchange(ex -> ex.anyExchange().permitAll())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }

    /**
     * API endpoints – JWT REQUIRED
     */
    @Bean
    @Order(1)
    SecurityWebFilterChain apiSecurity(ServerHttpSecurity http) {

        return http
                .securityMatcher(
                        ServerWebExchangeMatchers.pathMatchers("/api/**")
                )
                .authorizeExchange(ex -> ex
                        .pathMatchers("/api/transactions/**")
                        .hasAuthority("SCOPE_client.write")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt->{}))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }




}
