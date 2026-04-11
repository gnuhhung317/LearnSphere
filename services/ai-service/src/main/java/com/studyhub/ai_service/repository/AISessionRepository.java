package com.studyhub.ai_service.repository;

import com.studyhub.ai_service.entity.AISession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AISessionRepository extends JpaRepository<AISession, Long> {
    List<AISession> findAllByUserIdAndLearningSpaceIdOrderByUpdatedAtDesc(Long userId, Long learningSpaceId);

    Optional<AISession> findByIdAndUserId(Long id, Long userId);
}
