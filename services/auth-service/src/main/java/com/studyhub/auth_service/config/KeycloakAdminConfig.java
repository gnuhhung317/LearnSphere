package com.studyhub.auth_service.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakAdminConfig {

    @Value("${keycloak.server-url:localhost:8080}")
    private String keycloakUrl;

    @Value("${keycloak.realm:studyhub-backend}")
    private String keycloakRealm;

    @Value("${keycloak.admin-user:admin}")
    private String keycloakUsername;

    @Value("${keycloak.admin-password:admin}")
    private String keycloakPassword;

    @Value("${keycloak.client-secret}")
    private String keycloakClientSecret;

    @Value("${keycloak.client-id:studyhub-backend}")
    private String keycloakClientId;

    @Bean
    public Keycloak keycloak() {
        String clientCredentialType = "client_credentials";
        String passwordCredentialType = "password";
        return KeycloakBuilder.builder()
                .serverUrl(keycloakUrl)
                .realm(keycloakRealm)
                .username(keycloakUsername)
                .password(keycloakPassword)
                .clientId("admin-cli")
//                .clientSecret(keycloakClientSecret)
                .grantType(passwordCredentialType)
                .build();
    }
}
