package com.studyhub.learningservice.repository;

import com.studyhub.learningservice.domain.StudyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudyStatsRepository extends JpaRepository<StudyStats, Long> {

    Optional<StudyStats> findByUserId(String userId);
}
