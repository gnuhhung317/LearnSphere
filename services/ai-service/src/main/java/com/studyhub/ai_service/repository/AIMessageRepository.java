package com.studyhub.ai_service.repository;

import com.studyhub.ai_service.entity.AIMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AIMessageRepository extends JpaRepository<AIMessage, Long> {
    List<AIMessage> findAllBySessionIdOrderByCreatedAtAsc(Long sessionId);
}
