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
    public static Long getUserIdFromJwt() {
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
    public static Long getUserIdFromJwt(Jwt jwt) {
        if (jwt == null) {
            throw new IllegalArgumentException("JWT token cannot be null");
        }
        return extractUserIdFromJwt(jwt);
    }

    /**
     * Core logic to extract user ID from JWT token
     */
    private static Long extractUserIdFromJwt(Jwt jwt) {

        // Extract user_id from JWT claims (custom claim if exists)
        Long userId = jwt.getClaim("user_id");
        if (userId != null) {
            log.debug("Extracted userId from JWT claim: {}", userId);
            return userId;
        }

        // Try alternative claim names
        Integer userIdInt = jwt.getClaim("userId");
        if (userIdInt != null) {
            log.debug("Extracted userId from userId claim: {}", userIdInt);
            return userIdInt.longValue();
        }

        // Subject contains Keycloak UUID - fetch StudyHub userId from user-service
        String keycloakId = jwt.getSubject();
        if (keycloakId != null) {
            try {
                // Try parse as Long first (for backward compatibility)
                return Long.parseLong(keycloakId);
            } catch (NumberFormatException e) {
                // UUID format - fetch from user-service
                log.info("Fetching userId for Keycloak UUID: {}", keycloakId);
                try {
                    if (userClient == null) {
                        log.error("UserClient is null - cannot fetch user by Keycloak ID");
                        throw new IllegalStateException("UserClient not initialized");
                    }

                    UserClient.UserInfo userInfo = userClient.getUserByKeycloakId(keycloakId);
                    if (userInfo != null && userInfo.getId() != null) {
                        log.info("✅ Mapped Keycloak UUID {} to userId: {}", keycloakId, userInfo.getId());
                        return userInfo.getId();
                    } else {
                        log.error("UserClient returned null or empty UserInfo for Keycloak ID: {}", keycloakId);
                        throw new IllegalStateException("User not found for Keycloak ID: " + keycloakId);
                    }
                } catch (Exception ex) {
                    log.error("❌ Failed to fetch user by Keycloak ID: {} - Error: {}", keycloakId, ex.getMessage(), ex);
                    throw new IllegalStateException("Failed to resolve user from Keycloak ID: " + keycloakId + " - " + ex.getMessage());
                }
            }
        }

        throw new IllegalStateException("JWT token does not contain valid user identifier");
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
}
