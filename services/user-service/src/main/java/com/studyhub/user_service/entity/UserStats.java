package com.studyhub.user_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * UserStats entity for tracking user learning statistics Uses @MapsId to share
 * primary key with User entity
 */
@Entity
@Table(name = "user_stats")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStats {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "study_hours", nullable = false)
    private Integer studyHours = 0;

    @Column(name = "streak", nullable = false)
    private Integer streak = 0;

    @Column(name = "level", nullable = false)
    private Integer level = 1;

    @Column(name = "courses_completed", nullable = false)
    private Integer coursesCompleted = 0;

    @Column(name = "collaborations", nullable = false)
    private Integer collaborations = 0;

    @Column(name = "ai_insights", nullable = false)
    private Integer aiInsights = 0;

    @Column(name = "monthly_goal", nullable = false)
    private Integer monthlyGoal = 40; // Default 40 hours per month

    @Column(name = "monthly_progress", nullable = false)
    private Integer monthlyProgress = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
