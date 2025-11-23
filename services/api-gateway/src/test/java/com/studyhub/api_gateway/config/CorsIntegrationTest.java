package com.studyhub.api_gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("dev")
class CorsIntegrationTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void preflight_request_should_allow_local_frontend() {
        webTestClient.options()
                .uri("/api/users/me/profile")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueMatches("Access-Control-Allow-Origin", ".*localhost:3000.*")
                .expectHeader().exists("Access-Control-Allow-Credentials");
    }
}
