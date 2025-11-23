package com.studyhub.user_service.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class DevJwtHeaderFilterIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void dev_header_bearer_creates_jwt_subject() throws Exception {
        mvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer dev-user-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("dev-user-100"));
    }
}
