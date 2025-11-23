package com.studyhub.user_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating user stats
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatsRequest {

    
    @Min(value = 0, message = "Study hours must be at least 0")
       private Integer studyHours;

       @Min(value = 0, message = "Streak must be at least 0")
    private Integer streak;
    
    @Min(value = 1, message = "Level must be at least 1")
       private Integer level;

       @Min(value = 0, message = "Courses completed must be at least 0")
    private Integer coursesCompleted;
    
    @Min(value = 0, message = "Collaborations must be at least 0")
    private Integer collaborations;
    
    @Min(value = 0, message = "AI insights must be at least 0")
    private Integer aiInsights;

    @Min(value = 1, message = "Monthly goal must be at least 1")
    @Max(value = 744, message = "Monthly goal must not exceed 744 hours (31 days * 24 hours)")
    private Integer monthlyGoal;

    @Min(value = 0, message = "Monthly progress must be at least 0")
    private Integer monthlyProgress;
}
