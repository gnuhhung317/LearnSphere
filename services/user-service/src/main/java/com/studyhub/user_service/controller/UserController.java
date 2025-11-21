package com.studyhub.user_service.controller;

import com.studyhub.user_service.dto.CreateUserRequest;
import com.studyhub.user_service.dto.UserResponse;
import com.studyhub.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User Service is running!");
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Hello from User Service on port 8081!");
    }

    /**
     * Endpoint này trả về thông tin của người dùng đã được xác thực. Spring
     * Security sẽ tự động xác thực JWT token từ header Authorization. Nếu token
     * hợp lệ, nó sẽ trích xuất thông tin và inject vào tham số 'jwt'.
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        log.info("Getting current user info from JWT token");
        // jwt.getClaims() chứa tất cả thông tin trong token (subject, name, email...)
        return ResponseEntity.ok(jwt.getClaims());
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
