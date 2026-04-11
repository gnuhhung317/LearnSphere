package com.studyhub.learningservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntryDto {
    private String userId;
    private String username; // Will be "User" + last 4 chars of ID if unknown
    private String avatarUrl;
    private int xp;
    private int rank;
    private boolean isCurrentUser;
}
