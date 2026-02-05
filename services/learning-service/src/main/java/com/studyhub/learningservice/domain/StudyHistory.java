package com.studyhub.learningservice.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Tracks daily study history for heatmap visualization.
 * One record per user per day.
 */
@Entity
@Table(name = "study_history", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "study_date" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "study_date", nullable = false)
    private LocalDate studyDate;

    /**
     * Number of flashcards reviewed on this day
     */
    @Builder.Default
    @Column(name = "cards_reviewed")
    private Integer cardsReviewed = 0;

    /**
     * Number of quizzes completed on this day
     */
    @Builder.Default
    @Column(name = "quizzes_completed")
    private Integer quizzesCompleted = 0;

    /**
     * Total study time in minutes
     */
    @Builder.Default
    @Column(name = "study_minutes")
    private Integer studyMinutes = 0;

    /**
     * XP earned on this day
     */
    @Builder.Default
    @Column(name = "xp_earned")
    private Integer xpEarned = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Record a card review
     */
    public void addCardReview(int xp) {
        this.cardsReviewed++;
        this.xpEarned += xp;
    }

    /**
     * Record a quiz completion
     */
    public void addQuizCompletion(int xp) {
        this.quizzesCompleted++;
        this.xpEarned += xp;
    }
}
