package com.studyhub.user_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Development-only helper filter. If Authorization header contains a short
 * token (no dots) this filter will populate the SecurityContext with a Jwt
 * containing sub={token} so controllers using @AuthenticationPrincipal Jwt jwt
 * work in dev without Keycloak.
 */
@Component
@Profile("dev")
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class DevJwtHeaderFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(DevJwtHeaderFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                String token = auth.substring("Bearer ".length()).trim();
                // If token looks like a compact JWT (has dots) we don't create a fake Jwt here
                if (!token.contains(".")) {
                    log.debug("DevJwtHeaderFilter: creating dev Jwt for token='{}'", token);
                    Jwt jwt = new Jwt(token, Instant.now(), Instant.now().plusSeconds(3600), Map.of("alg", "none"), Map.of("sub", token));

                    // create a simple authentication with ROLE_USER
                    JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_USER")));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
