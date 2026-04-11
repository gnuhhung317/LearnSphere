package com.studyhub.learningservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for daily goals configuration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyGoalDto {
    private int cardsGoal;
    private int quizzesGoal;
    private int xpGoal;
    private int focusMinutesGoal;
    private boolean remindersEnabled;
    private int reminderHour;
}
