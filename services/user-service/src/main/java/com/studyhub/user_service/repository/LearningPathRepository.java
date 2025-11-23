package com.studyhub.user_service.repository;

import com.studyhub.user_service.entity.LearningPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningPathRepository extends JpaRepository<LearningPath, Long> {

    
    /**
     * Find all active learning paths for a user
     */
    @Query("SELECT lp FROM LearningPath lp WHERE lp.user.userId = :userId AND lp.isActive = true ORDER BY lp.createdAt DESC")

       List<LearningPath> findByUserIdAndIsActiveTrue(@Param("userId") Long userId);

    /**
     * Find all learning paths for a user (including inactive)
     */
    @Query("SELECT lp FROM LearningPath lp WHERE lp.user.userId = :userId ORDER BY lp.createdAt DESC")
    List<LearningPath> findByUserId(@Param("userId") Long userId);
}
