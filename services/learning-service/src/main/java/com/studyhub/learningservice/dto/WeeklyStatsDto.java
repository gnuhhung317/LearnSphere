package com.studyhub.learningservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for weekly stats breakdown
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyStatsDto {
    private String weekLabel; // e.g., "Jan 1-7"
    private int cardsReviewed;
    private int quizzesCompleted;
    private int xpEarned;
    private int studyDays; // number of days studied in that week
}
