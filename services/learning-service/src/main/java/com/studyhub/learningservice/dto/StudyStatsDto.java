package com.studyhub.learningservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyStatsDto {
    private long totalDueCards;
    private int cardsReviewedToday;
    private int currentStreak;
    private int longestStreak;
    private long totalCardsReviewed;
    private LocalDate lastStudyDate;
}
