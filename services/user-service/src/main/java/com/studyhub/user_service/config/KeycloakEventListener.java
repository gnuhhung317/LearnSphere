package com.studyhub.user_service.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyhub.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka listener for Keycloak user events Syncs user profile data when users
 * register/update in Keycloak
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakEventListener {

//    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();

}
