package com.studyhub.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for user summary in search results and follower/following lists
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDto {

    private Long id;
    private String username;
    private String fullName;
    private String avatarUrl;
    private String bio;
    private String location;
    private Integer followersCount;
    private Integer followingCount;
    private Boolean isFollowing; // Whether current user is following this user
    private LocalDateTime joinedAt;
}
