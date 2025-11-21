package com.studyhub.user_service.service;

import com.studyhub.user_service.dto.CreateUserRequest;
import com.studyhub.user_service.dto.UserResponse;
import com.studyhub.user_service.entity.User;

import java.util.Map;

/**
 * Service interface for user operations
 */
public interface UserService {

    /**
     * create user profile
     */
    UserResponse createUser(CreateUserRequest createUserRequest);

    /**
     * Get user by Keycloak user ID
     */
    User getUserByKeycloakId(String keycloakUserId);

    /**
     * Get user by internal ID
     */
    User getUserById(Long id);

    /**
     * Get user by email
     */
    User getUserByEmail(String email);

    /**
     * Update user preferences (language, theme, notifications, etc.)
     */
    User updateUserPreferences(String keycloakUserId, Map<String, Object> preferences);
}
