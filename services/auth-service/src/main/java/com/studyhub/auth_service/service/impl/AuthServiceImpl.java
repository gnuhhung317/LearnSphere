package com.studyhub.auth_service.service.impl;

import com.studyhub.auth_service.client.UserClient;
import com.studyhub.auth_service.dto.interal_request.CreateUserRequest;
import com.studyhub.auth_service.dto.request.LoginRequest;
import com.studyhub.auth_service.dto.request.RegisterRequest;
import com.studyhub.auth_service.dto.response.LoginResponse;
import com.studyhub.auth_service.dto.response.RegisterResponse;
import com.studyhub.auth_service.service.AuthService;
import com.studyhub.auth_service.service.KeycloakService;
import com.studyhub.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final KeycloakService keycloakService;
    private final UserClient userClient;

    @Override
    public RegisterResponse register(RegisterRequest request) {
        CreateUserRequest createUserRequest = keycloakService.createUser(request);
        try {
            RegisterResponse registerResponse = userClient.register(createUserRequest);

            // Build and return success response
            return RegisterResponse.builder()
                    .userId(registerResponse.getUserId())
                    .keycloakUserId(registerResponse.getKeycloakUserId())
                    .email(registerResponse.getEmail())
                    .fullName(registerResponse.getFullName())
                    .message("User registered successfully")
                    .build();
        } catch (Exception e) {
            keycloakService.deleteUser(createUserRequest.getKeycloakUserId());
            throw new BusinessException("USER_INTERNAL_ERROR");
        }
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        return keycloakService.login(request);
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        return keycloakService.refreshToken(refreshToken);
    }
}
