package com.studyhub.learningservice.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * User's daily study goals configuration and progress
 */
@Entity
@Table(name = "daily_goals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    /**
     * Daily target for flashcards to review
     */
    @Builder.Default
    @Column(name = "cards_goal")
    private Integer cardsGoal = 20;

    /**
     * Daily target for quizzes to complete
     */
    @Builder.Default
    @Column(name = "quizzes_goal")
    private Integer quizzesGoal = 2;

    /**
     * Daily target for XP to earn
     */
    @Builder.Default
    @Column(name = "xp_goal")
    private Integer xpGoal = 100;

    /**
     * Daily target for focus minutes (Pomodoro)
     */
    @Builder.Default
    @Column(name = "focus_minutes_goal")
    private Integer focusMinutesGoal = 60;

    /**
     * Whether to receive daily reminder notifications
     */
    @Builder.Default
    @Column(name = "reminders_enabled")
    private Boolean remindersEnabled = true;

    /**
     * Preferred reminder time (hour of day, 0-23)
     */
    @Builder.Default
    @Column(name = "reminder_hour")
    private Integer reminderHour = 20;

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
}
