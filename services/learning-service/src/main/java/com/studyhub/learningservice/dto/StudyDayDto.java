package com.studyhub.learningservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for a single day's study data (for heatmap)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyDayDto {
    private LocalDate date;
    private int cardsReviewed;
    private int quizzesCompleted;
    private int xpEarned;

    /**
     * Activity level for heatmap coloring (0-4)
     * 0 = no activity, 4 = high activity
     */
    private int level;
}
