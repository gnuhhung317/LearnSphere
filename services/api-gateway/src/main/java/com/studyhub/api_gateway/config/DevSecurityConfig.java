package com.studyhub.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;

/**
 * Security configuration for API Gateway - Development profile Enables OAuth2
 * Resource Server with Keycloak JWT validation
 */
@Configuration
@EnableWebFluxSecurity
@Profile("dev")
public class DevSecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(withDefaults()) // Enable CORS for frontend integration
                .authorizeExchange(exchanges -> exchanges
                // Allow CORS preflight
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                // Public endpoints (no authentication required)
                .pathMatchers(
                        "/api/auth/**",
                        "/realms/**",
                        "/admin/**",
                        "/actuator/health",
                        "/fallback/**"
                ).permitAll()
                // Protected endpoints (require authentication)
                .pathMatchers(
                        "/api/users/**",
                        "/api/chat/**",
                        "/api/media/**"
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
