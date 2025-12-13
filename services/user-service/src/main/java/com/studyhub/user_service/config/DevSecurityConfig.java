package com.studyhub.user_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Profile("dev")  // Only active in dev profile
public class DevSecurityConfig {

    @Bean
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/users/health", "/api/users/test").permitAll() // Public endpoints
                .anyRequest().permitAll() // All other endpoints require authentication
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(Customizer.withDefaults())
                );

        return http.build();
    }
}
