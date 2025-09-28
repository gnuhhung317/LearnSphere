package com.studyhub.api_gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Gateway Configuration for Rate Limiting and Custom Filters Based on Context7
 * best practices for Spring Cloud Gateway
 */
@Configuration
public class GatewayConfig {

    /**
     * Rate limiting by user IP address Used as fallback when user is not
     * authenticated. This is marked as @Primary to serve as the default
     * KeyResolver.
     */
    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(getClientIP(exchange));
    }

    /**
     * Rate limiting by authenticated user ID Provides per-user rate limiting
     * for authenticated requests
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> exchange.getPrincipal()
                .cast(org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken.class)
                .map(principal -> principal.getToken().getClaimAsString("sub"))
                .switchIfEmpty(Mono.just(getClientIP(exchange)));
    }

    /**
     * Rate limiting by API endpoint Provides endpoint-specific rate limiting
     */
    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> {
            String path = exchange.getRequest().getPath().value();
            String method = exchange.getRequest().getMethod().name();
            return Mono.just(method + ":" + path);
        };
    }

    /**
     * Combined rate limiting strategy Uses user ID for authenticated requests,
     * IP for anonymous
     */
    @Bean
    public KeyResolver combinedKeyResolver() {
        return exchange -> {
            // Try to get user ID from JWT token
            return exchange.getPrincipal()
                    .cast(org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken.class)
                    .map(principal -> "user:" + principal.getToken().getClaimAsString("sub"))
                    .switchIfEmpty(Mono.just("ip:" + getClientIP(exchange)));
        };
    }

    /**
     * Extract client IP address from request Handles X-Forwarded-For and other
     * proxy headers
     */
    private String getClientIP(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return Objects.requireNonNull(
                exchange.getRequest().getRemoteAddress()
        ).getAddress().getHostAddress();
    }
}
