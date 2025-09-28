package com.studyhub.api_gateway.controller;

import com.studyhub.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check and status endpoints
 */
@RestController
public class HealthController {

    @GetMapping("/")
    public Mono<ApiResponse<Map<String, Object>>> root() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "StudyHub API Gateway");
        info.put("status", "UP");
        info.put("timestamp", LocalDateTime.now());
        info.put("version", "1.0.0");

        return Mono.just(ApiResponse.success("API Gateway is running", info));
    }

    @GetMapping("/health")
    public Mono<ApiResponse<Map<String, String>>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", LocalDateTime.now().toString());

        return Mono.just(ApiResponse.success("Gateway is healthy", status));
    }
}
