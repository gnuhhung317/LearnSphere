package com.studyhub.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyhub.user_service.dto.CreateLearningPathRequest;
import com.studyhub.user_service.dto.LearningPathDto;
import com.studyhub.user_service.dto.UserProfileViewResponse;
import com.studyhub.user_service.service.UserProfileService;
import com.studyhub.user_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserProfileService userProfileService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getMyProfile_returnsProfile() throws Exception {
        UserProfileViewResponse resp = new UserProfileViewResponse();
        resp.setId(42L);
        resp.setFullName("Test User");

        when(userProfileService.getUserProfileView("kc-42")).thenReturn(resp);

        mockMvc.perform(get("/api/users/me/profile")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.claim("sub", "kc-42"))))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(resp)));
    }

    @Test
    void getMyProfile_returns401_whenNoJwt() throws Exception {
        mockMvc.perform(get("/api/users/me/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createLearningPath_returnsCreated() throws Exception {
        CreateLearningPathRequest req = new CreateLearningPathRequest();
        req.setName("New Path");
        req.setDescription("desc");

        LearningPathDto dto = new LearningPathDto();
        dto.setId(100L);
        dto.setName("New Path");

        when(userProfileService.createLearningPath(any(), any(CreateLearningPathRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/users/me/learning-paths")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(j -> j.claim("sub", "kc-42")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(dto)));
    }
}
