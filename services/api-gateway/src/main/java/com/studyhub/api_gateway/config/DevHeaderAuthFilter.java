package com.studyhub.api_gateway.config;

import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Development helper filter that enforces presence of Authorization header for protected routes.
 * This prevents unauthenticated requests from reaching downstream services when the dev profile
 * disables full OAuth2 validation.
 */
@Component
@Profile("dev")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DevHeaderAuthFilter implements WebFilter {

    // Protected prefix list - keep in sync with SecurityConfig's protected paths
    private final List<String> protectedPrefixes = List.of(
            "/api/users",
            "/api/chat",
            "/api/media"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        // Allow CORS preflight to pass through: do not require Authorization header for OPTIONS
        if (exchange.getRequest().getMethod() != null && exchange.getRequest().getMethod().name().equalsIgnoreCase("OPTIONS")) {
            return chain.filter(exchange);
        }

        // If the request is for a protected path, require Authorization header
        for (String p : protectedPrefixes) {
            if (path.startsWith(p)) {
                HttpHeaders headers = exchange.getRequest().getHeaders();
                if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    String body = "{\"message\":\"Missing Authorization header\"}";
                    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                    return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
                }
            }
        }

        return chain.filter(exchange);
    }
}
