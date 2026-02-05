package com.studyhub.learningservice.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Tracks user study statistics including streak data.
 * One record per user.
 */
@Entity
@Table(name = "study_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    /**
     * Current streak count (consecutive days of study)
     */
    @Builder.Default
    @Column(name = "current_streak")
    private Integer currentStreak = 0;

    /**
     * Longest streak ever achieved
     */
    @Builder.Default
    @Column(name = "longest_streak")
    private Integer longestStreak = 0;

    /**
     * Last date user completed at least one review
     */
    @Column(name = "last_study_date")
    private LocalDate lastStudyDate;

    /**
     * Total cards reviewed all time
     */
    @Builder.Default
    @Column(name = "total_cards_reviewed")
    private Long totalCardsReviewed = 0L;

    /**
     * Cards reviewed today
     */
    @Builder.Default
    @Column(name = "cards_reviewed_today")
    private Integer cardsReviewedToday = 0;

    /**
     * The date for which cardsReviewedToday is counted
     */
    @Column(name = "today_date")
    private LocalDate todayDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (todayDate == null) {
            todayDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Record a study session. Updates streak and daily counts.
     */
    public void recordStudySession() {
        LocalDate today = LocalDate.now();

        // Reset today's count if it's a new day
        if (todayDate == null || !todayDate.equals(today)) {
            cardsReviewedToday = 0;
            todayDate = today;
        }

        cardsReviewedToday++;
        totalCardsReviewed++;

        // Update streak logic
        if (lastStudyDate == null) {
            // First ever study
            currentStreak = 1;
        } else if (lastStudyDate.equals(today)) {
            // Already studied today, streak unchanged
        } else if (lastStudyDate.equals(today.minusDays(1))) {
            // Studied yesterday, increment streak
            currentStreak++;
        } else {
            // Streak broken, reset to 1
            currentStreak = 1;
        }

        // Update longest streak
        if (currentStreak > longestStreak) {
            longestStreak = currentStreak;
        }

        lastStudyDate = today;
    }
}
