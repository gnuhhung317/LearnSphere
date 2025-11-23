package com.studyhub.user_service.dto;

import com.studyhub.common.constant.enums.SocialPlatform;
import com.studyhub.common.constant.enums.SupportedLanguage;
import com.studyhub.common.constant.enums.Theme;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive DTO for user profile view Aggregates data from User,
 * serProfile, UserStats, and LearningPaths
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileViewResponse {

    private Long id;
    private String keycloakUserId;
    private String username;
    private String fullName;
    private String email;
    private String location;

    // Profile information
    private String avatarUrl;
    private String bio;
    private String title;
    private Map<SocialPlatform, String> socialLinks;

    // User preferences
    private Theme theme;
    private SupportedLanguage language;
    private Map<String, Boolean> notifications;
    private Map<String, Boolean> accessibility;
    private Map<String, Object> privacy;

    // User statistics
    private UserStatsDto stats;

    // Learning paths
    private List<LearningPathDto> learningPaths;

    // Social connections
    private Integer followersCount;
    private Integer followingCount;
    private Boolean isFollowing; // Whether current user is following this user

    // Account status
    private String status;
    private Boolean isVerified;
    private Boolean isActive;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
