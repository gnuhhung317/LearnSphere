package com.studyhub.api_gateway.controller;

import com.studyhub.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Fallback controllers for when services are unavailable Enhanced with Context7
 * best practices for graceful degradation
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/users")
    public Mono<ApiResponse<String>> userServiceFallback() {
        ApiResponse<String> response = ApiResponse.error(
                "User service is temporarily unavailable. Please try again later.",
                List.of("USERS_SERVICE_UNAVAILABLE")
        );
        return Mono.just(response);
    }

    @GetMapping("/auth")
    public Mono<ApiResponse<String>> authServiceFallback() {
        ApiResponse<String> response = ApiResponse.error(
                "Authentication service is temporarily unavailable. Please try again later.",
                List.of("AUTH_SERVICE_UNAVAILABLE")
        );
        return Mono.just(response);
    }

    @GetMapping("/chat")
    public Mono<ApiResponse<String>> chatServiceFallback() {
        ApiResponse<String> response = ApiResponse.error(
                "Chat service is temporarily unavailable. Please try again later.",
                List.of("CHAT_SERVICE_UNAVAILABLE")
        );
        return Mono.just(response);
    }

    @GetMapping("/media")
    public Mono<ApiResponse<String>> mediaServiceFallback() {
        ApiResponse<String> response = ApiResponse.error(
                "Media service is temporarily unavailable. Please try again later.",
                List.of("MEDIA_SERVICE_UNAVAILABLE")
        );
        return Mono.just(response);
    }

    @GetMapping("/realtime")
    public Mono<ApiResponse<String>> realtimeServiceFallback() {
        ApiResponse<String> response = ApiResponse.error(
                "Real-time service is temporarily unavailable. Please try again later.",
                List.of("REALTIME_SERVICE_UNAVAILABLE")
        );
        return Mono.just(response);
    }

    @GetMapping("/ai")
    public Mono<ApiResponse<String>> aiServiceFallback() {
        ApiResponse<String> response = ApiResponse.error(
                "AI service is temporarily unavailable. Please try again later.",
                List.of("AI_SERVICE_UNAVAILABLE")
        );
        return Mono.just(response);
    }

    @GetMapping("/search")
    public Mono<ApiResponse<String>> searchServiceFallback() {
        ApiResponse<String> response = ApiResponse.error(
                "Search service is temporarily unavailable. Please try again later.",
                List.of("SEARCH_SERVICE_UNAVAILABLE")
        );
        return Mono.just(response);
    }

    @GetMapping("/generic")
    public Mono<ApiResponse<String>> genericFallback() {
        ApiResponse<String> response = ApiResponse.error(
                "Service is temporarily unavailable. Please try again later.",
                List.of("SERVICE_UNAVAILABLE")
        );
        return Mono.just(response);
    }
}
