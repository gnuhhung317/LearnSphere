package com.studyhub.auth_service.service;

import com.studyhub.auth_service.dto.request.LoginRequest;
import com.studyhub.auth_service.dto.request.RegisterRequest;
import com.studyhub.auth_service.dto.response.LoginResponse;
import com.studyhub.auth_service.dto.response.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    LoginResponse refreshToken(String refreshToken);
}
