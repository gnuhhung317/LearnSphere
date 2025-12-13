package com.studyhub.user_service.service;

import com.studyhub.user_service.dto.*;
import com.studyhub.user_service.entity.LearningPath;

import java.util.List;

/**
 * Service interface for user profile operations
 */
public interface UserProfileService {

    /**
     * Get comprehensive user profile view by Keycloak user ID Aggregates User,
     * UserProfile, UserStats, and LearningPaths
     */
    UserProfileViewResponse getUserProfileView(String keycloakUserId);

    /**
     * Get comprehensive user profile view by user ID (internal DB ID) Used for
     * service-to-service calls
     */
    UserProfileViewResponse getUserProfileViewById(Long userId);

    /**
     * Get comprehensive user profile view by Keycloak user ID Alias for
     * getUserProfileView for consistency
     */
    UserProfileViewResponse getUserProfileByKeycloakId(String keycloakUserId);

    /**
     * Update user profile information
     */
    UserProfileViewResponse updateUserProfile(String keycloakUserId, UpdateUserProfileRequest request);

    /**

          ** Update user statistics
     */
    UserStatsDto updateUserStats(String keycloakUserId, UpdateStatsRequest request);

    /**
     * Update monthly learning goal
     */
    UserStatsDto updateMonthlyGoal(String keycloakUserId, Integer monthlyGoal);

    /**
     * Create a new learning path for user
     */
    LearningPathDto createLearningPath(String keycloakUserId, CreateLearningPathRequest request);

    /**
     * Get all learning paths for a user

          **/
    List<LearningPathDto> getUserLearningPaths(String keycloakUserId);

    /**
     * Update learning path progress
     */
    LearningPathDto updateLearningPathProgress(String keycloakUserId, Long pathId, Integer progress);

    /**
     * Delete a learning path
     */
    void deleteLearningPath(String keycloakUserId, Long pathId);
}
