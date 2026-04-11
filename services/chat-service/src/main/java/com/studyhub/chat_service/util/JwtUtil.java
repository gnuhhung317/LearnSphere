package com.studyhub.chat_service.util;

import com.studyhub.chat_service.client.UserClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Utility class for extracting user information from JWT tokens
 */
@Slf4j
@Component
public class JwtUtil {

    private static UserClient userClient;

    @Autowired
    public void setUserClient(UserClient userClient) {
        JwtUtil.userClient = userClient;
    }

    /**
     * Extract user ID from JWT token in SecurityContext
     *
     * @return userId from JWT claims
     * @throws IllegalStateException if no valid authentication is found
     */
    public static String  getUserIdFromJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("No authenticated user found. JWT authentication is required.");
        }

        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("Authentication principal is not a JWT token");
        }

        return extractUserIdFromJwt(jwt);
    }

    /**
     * Extract user ID from provided JWT token (for WebSocket where Principal is passed)
     *
     * @param jwt the JWT token
     * @return userId from JWT claims
     */
    public static String getUserIdFromJwt(Jwt jwt) {
        if (jwt == null) {
            throw new IllegalArgumentException("JWT token cannot be null");
        }
        return extractUserIdFromJwt(jwt);
    }

    /**
     * Core logic to extract user ID from JWT token
     */
    private static String extractUserIdFromJwt(Jwt jwt) {


        // Subject contains Keycloak UUID - fetch StudyHub userId from user-service
        return jwt.getSubject();

    }

    /**
     * Extract username from JWT token
     *
     * @throws IllegalStateException if no valid authentication is found
     */
    public static String getUsernameFromJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("No authenticated user found. JWT authentication is required.");
        }

        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("Authentication principal is not a JWT token");
        }

        // Try various username claims
        String username = jwt.getClaim("preferred_username");
        if (username != null) {
            return username;
        }

        username = jwt.getClaim("username");
        if (username != null) {
            return username;
        }

        // Fallback to subject
        String subject = jwt.getSubject();
        if (subject != null) {
            return subject;
        }

        throw new IllegalStateException("JWT token does not contain username claim");
    }

    public static String getJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("No authenticated user found. JWT authentication is required.");
        }

        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("Authentication principal is not a JWT token");
        }
        return jwt.getTokenValue();
    }
}
