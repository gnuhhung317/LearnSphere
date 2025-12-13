package com.studyhub.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;

/**
 * Security configuration for API Gateway - Production profile Enables OAuth2
 * Resource Server with JWT validation
 */
@Configuration
@EnableWebFluxSecurity
@Profile("!dev")
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                // Allow CORS preflight
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                // Public endpoints (no authentication required)
                .pathMatchers(
                        "/api/v1/auth/**",
                        "/realms/**",
                        "/admin/**",
                        "/actuator/health",
                        "/fallback/**"
                ).permitAll()
                // Protected endpoints (require authentication)
                .pathMatchers(
                        "/api/v1/users/**",
                        "/api/v1/chat/**",
                        "/api/v1/media/**"
                ).authenticated()
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
            return jwt.getClaimAsStringList("roles").stream()
                    .map(role -> "ROLE_" + role.toUpperCase())
                    .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                    .collect(java.util.stream.Collectors.toList());
        });
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }
}
