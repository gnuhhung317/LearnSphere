package com.studyhub.learningservice.repository;

import com.studyhub.learningservice.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByResourceId(Long resourceId);
}
