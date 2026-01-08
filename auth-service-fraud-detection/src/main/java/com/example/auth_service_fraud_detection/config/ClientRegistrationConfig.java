package com.example.auth_service_fraud_detection.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.util.UUID;

@Configuration
public class ClientRegistrationConfig {

    @Bean
    RegisteredClientRepository registeredClientRepository(
            PasswordEncoder encoder,
            JdbcTemplate jdbcTemplate,
            @Value("${fraud.oauth.client-id}") String clientId,
            @Value("${fraud.oauth.client-secret}")String clientSecret
            ) {
        JdbcRegisteredClientRepository repo =
                new JdbcRegisteredClientRepository(jdbcTemplate);

        // Register client once (idempotent)
        if (repo.findByClientId("fraud-client") == null) {

            RegisteredClient client =
                    RegisteredClient.withId("fraud-client-id")
                            .clientId(clientId)
                            .clientSecret(encoder.encode(clientSecret))
                            .clientName("Fraud Detection Service")
                            .clientAuthenticationMethod(
                                    ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                            .authorizationGrantType(
                                    AuthorizationGrantType.CLIENT_CREDENTIALS)
                            .scope("client.read")
                            .scope("client.write")
                            .tokenSettings(TokenSettings.builder()
                                    .accessTokenTimeToLive(Duration.ofMinutes(15))
                                    .build())
                            .build();

            repo.save(client);
        }

        return repo;

    }

}
