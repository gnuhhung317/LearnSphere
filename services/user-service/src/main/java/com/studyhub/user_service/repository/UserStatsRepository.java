package com.studyhub.user_service.repository;

import com.studyhub.user_service.entity.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStatsRepository extends JpaRepository<UserStats, Long> {

    
    /**
     * Find user stats by user ID Note: UserStats uses @MapsId, so the ID is the
     * same as User's ID
    */

     
    Optional<UserStats> findById(Long userId);
}
