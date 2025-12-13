package com.studyhub.auth_service.controller;

import com.studyhub.auth_service.dto.request.LoginRequest;
import com.studyhub.auth_service.dto.request.RefreshTokenRequest;
import com.studyhub.auth_service.dto.request.RegisterRequest;
import com.studyhub.auth_service.dto.response.LoginResponse;
import com.studyhub.auth_service.dto.response.RegisterResponse;
import com.studyhub.auth_service.service.AuthService;
import com.studyhub.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        List<String> roles = realmAccess != null
                ? (List<String>) realmAccess.get("roles")
                : List.of();

        return Map.of("username", jwt.getClaimAsString("preferred_username"),
                 "email", jwt.getClaimAsString("email"),
                "roles", roles);
    }

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request.getRefreshToken());
        return ApiResponse.success(response);
    }
}
