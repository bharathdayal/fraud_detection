package com.example.auth_service_fraud_detection.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

@Configuration
public class AuthorizationServerSettingsConfig {

    @Bean
    AuthorizationServerSettings authorizationServerSettings() {
            return AuthorizationServerSettings
                    .builder()
                    .issuer("http://localhost:9001")
                    .build();
    }
}
