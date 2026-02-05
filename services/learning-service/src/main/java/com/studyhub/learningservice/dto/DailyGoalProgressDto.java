package com.studyhub.learningservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for today's goal progress
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyGoalProgressDto {
    // Goals
    private int cardsGoal;
    private int quizzesGoal;
    private int xpGoal;
    private int focusMinutesGoal;

    // Progress
    private int cardsCompleted;
    private int quizzesCompleted;
    private int xpEarned;
    private int focusMinutesCompleted;

    // Calculated
    private int cardsPercentage;
    private int quizzesPercentage;
    private int xpPercentage;
    private int focusPercentage;
    private int overallPercentage;
    private boolean goalsMet;
}
