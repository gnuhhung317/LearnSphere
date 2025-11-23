package com.studyhub.api_gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("dev")
class DevHeaderAuthFilterTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void protectedRouteWithoutAuthReturns401() {
        webTestClient.get()
                .uri("/api/users/me/profile")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void publicRouteStillAccessible() {
        // Health endpoint may include checks for external infra (redis/rabbit) in tests,
        // assert that it is NOT returning Unauthorized (401) so the public route remains open
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().value(status -> {
                    // ensure filter did not block the request
                    org.junit.jupiter.api.Assertions.assertNotEquals(401, status);
                });
    }
}
