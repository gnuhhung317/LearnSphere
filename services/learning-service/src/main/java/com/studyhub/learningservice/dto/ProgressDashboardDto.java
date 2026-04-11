package com.studyhub.learningservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for progress dashboard data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressDashboardDto {

    // Streak info
    private int currentStreak;
    private int longestStreak;

    // Total stats
    private long totalCardsReviewed;
    private long totalQuizzesCompleted;
    private long totalXpEarned;
    private long totalStudyDays;

    // This week stats
    private int weekCardsReviewed;
    private int weekQuizzesCompleted;
    private int weekXpEarned;

    // Today stats
    private int todayCardsReviewed;
    private int todayQuizzesCompleted;

    // Heatmap data (last 365 days)
    private List<StudyDayDto> heatmapData;

    // Weekly breakdown (last 12 weeks)
    private List<WeeklyStatsDto> weeklyStats;
}
