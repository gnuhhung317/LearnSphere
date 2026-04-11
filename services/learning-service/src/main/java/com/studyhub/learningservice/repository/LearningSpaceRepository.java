package com.studyhub.learningservice.repository;

import com.studyhub.learningservice.domain.LearningSpace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningSpaceRepository extends JpaRepository<LearningSpace, Long> {
    List<LearningSpace> findByUserId(String userId);

    List<LearningSpace> findByTitleContainingIgnoreCaseAndUserId(String title, String userId);

    List<LearningSpace> findTop5ByUserIdOrderByUpdatedAtDesc(String userId);
}
