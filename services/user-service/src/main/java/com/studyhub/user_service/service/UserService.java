package com.studyhub.user_service.service;

import com.studyhub.user_service.dto.CreateUserRequest;
import com.studyhub.user_service.dto.UserResponse;
import com.studyhub.user_service.dto.UserInfo;
import com.studyhub.user_service.entity.User;

import java.util.List;
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

    List<UserInfo> getBasicBulk(List<String> keycloakIds);
    UserInfo getBasic(String keycloakUserId);
    /**
     * Update user preferences (language, theme, notifications, etc.)
     */
    User updateUserPreferences(String keycloakUserId, Map<String, Object> preferences);

    /**
     * Follow a user
     */
    void followUser(String followerKeycloakId, Long followedUserId);

    /**
     * Unfollow a user
     */
    void unfollowUser(String followerKeycloakId, Long followedUserId);

    /**
     * Check if follower is following followed user
     */
    boolean isFollowing(Long followerId, Long followedId);

    /**
     * Search users by query string (searches username, fullName, location)
     */
    java.util.List<com.studyhub.user_service.dto.UserSummaryDto> searchUsers(
            String query,
            String currentUserKeycloakId
    );

    /**
     * Get followers list for a user
     */
    java.util.List<com.studyhub.user_service.dto.UserSummaryDto> getFollowers(
            Long userId,
            String currentUserKeycloakId
    );

    /**
     * Get following list for a user
     */
    java.util.List<com.studyhub.user_service.dto.UserSummaryDto> getFollowing(
            Long userId,
            String currentUserKeycloakId
    );
}
