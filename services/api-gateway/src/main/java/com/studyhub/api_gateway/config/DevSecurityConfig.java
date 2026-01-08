package com.studyhub.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;

/**
 * Security configuration for API Gateway - Development profile 
 * Uses two security chains: one for WebSocket (no OAuth2), one for REST API (with OAuth2)
 */
@Configuration
@EnableWebFluxSecurity
@Profile("dev")
public class DevSecurityConfig {

    /**
     * Security chain for WebSocket endpoints - NO OAuth2, just permitAll
     * Higher priority (@Order(1)) to be checked first
     */
    @Bean
    @Order(1)
    public SecurityWebFilterChain webSocketSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher("/api/v1/**/ws{suffix:.*}"))
                .csrf(csrf -> csrf.disable())
                .cors(withDefaults())
                .authorizeExchange(exchanges -> exchanges
                    .anyExchange().permitAll()
                )
                .build();
    }

    /**
     * Security chain for REST API endpoints - OAuth2 Resource Server with JWT
     * Lower priority (@Order(2)) to handle all other requests
     */
    @Bean
    @Order(2)
    public SecurityWebFilterChain apiSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(withDefaults())
                .authorizeExchange(exchanges -> exchanges
                // Allow CORS preflight
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                // Public endpoints (no authentication required)
                .pathMatchers(
                        "/api/v1/auth/**",
                        "/realms/**",
                        "/admin/**",
                        "/actuator/health",
                        "/fallback/**",
                        "/api/v1/realtime/**"
                ).permitAll()
                // Protected endpoints (require authentication)
                .pathMatchers(
                        "/api/v1/users/**",
                        "/api/v1/chat/v1/**" // Chat REST API requires auth
                ).authenticated()
                .pathMatchers(
                        "/api/v1/media/files/*/download",
                        "/api/v1/media/variants/*/download"
                ).permitAll()
                .pathMatchers("/api/v1/media/**").authenticated()
                .anyExchange().permitAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> jwt
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                    )
                )
                .build();
    }

    @Bean
    public ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Extract authorities/roles from JWT claims
            var roles = jwt.getClaimAsStringList("roles");
            if (roles == null) {
                return java.util.Collections.emptyList();
            }
            return roles.stream()
                    .map(role -> "ROLE_" + role.toUpperCase())
                    .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                    .collect(java.util.stream.Collectors.toList());
        });
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }
}
