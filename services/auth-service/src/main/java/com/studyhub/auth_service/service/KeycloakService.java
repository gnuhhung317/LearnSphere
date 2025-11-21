package com.studyhub.auth_service.service;

import com.studyhub.auth_service.dto.interal_request.CreateUserRequest;
import com.studyhub.auth_service.dto.request.LoginRequest;
import com.studyhub.auth_service.dto.request.RegisterRequest;
import com.studyhub.auth_service.dto.response.LoginResponse;
import com.studyhub.auth_service.dto.response.RegisterResponse;

public interface KeycloakService {
    CreateUserRequest createUser(RegisterRequest request);
    void deleteUser(String keycloakUserId);
    void updateUser(String keycloakUserId, RegisterResponse request);
    LoginResponse login(LoginRequest request);
}
