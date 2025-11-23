package com.studyhub.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDto {

    private Integer studyHours;
    private Integer streak;
    private Integer level;
    private Integer coursesCompleted;
    private Integer collaborations;
    private Integer aiInsights;
    private Integer monthlyGoal;
    private Integer monthlyProgress;
}
