package com.studyhub.learningservice.repository;

import com.studyhub.learningservice.domain.StudyHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudyHistoryRepository extends JpaRepository<StudyHistory, Long> {

        /**
         * Find study history for a specific user and date
         */
        Optional<StudyHistory> findByUserIdAndStudyDate(String userId, LocalDate studyDate);

        /**
         * Get study history for a date range (for heatmap)
         */
        @Query("SELECT h FROM StudyHistory h " +
                        "WHERE h.userId = :userId " +
                        "AND h.studyDate >= :startDate " +
                        "AND h.studyDate <= :endDate " +
                        "ORDER BY h.studyDate ASC")
        List<StudyHistory> findByUserIdAndDateRange(
                        @Param("userId") String userId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        /**
         * Get total stats for a user
         */
        @Query("SELECT COALESCE(SUM(h.cardsReviewed), 0) FROM StudyHistory h WHERE h.userId = :userId")
        Long getTotalCardsReviewed(@Param("userId") String userId);

        @Query("SELECT COALESCE(SUM(h.quizzesCompleted), 0) FROM StudyHistory h WHERE h.userId = :userId")
        Long getTotalQuizzesCompleted(@Param("userId") String userId);

        @Query("SELECT COALESCE(SUM(h.xpEarned), 0) FROM StudyHistory h WHERE h.userId = :userId")
        Long getTotalXpEarned(@Param("userId") String userId);

        @Query("SELECT COUNT(h) FROM StudyHistory h WHERE h.userId = :userId AND h.cardsReviewed > 0")
        Long getTotalStudyDays(@Param("userId") String userId);

        @Query("SELECT h.userId as userId, SUM(h.xpEarned) as totalXp " +
                        "FROM StudyHistory h " +
                        "WHERE h.studyDate >= :startDate " +
                        "GROUP BY h.userId " +
                        "ORDER BY totalXp DESC")
        List<LeaderboardProjection> getTopUsersByXp(@Param("startDate") LocalDate startDate);
}
