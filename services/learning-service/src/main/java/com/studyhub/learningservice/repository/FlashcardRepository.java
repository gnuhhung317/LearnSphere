package com.studyhub.learningservice.repository;

import com.studyhub.learningservice.domain.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {

    @Query("SELECT COUNT(f) FROM Flashcard f JOIN f.deck d JOIN d.resource r JOIN r.section s JOIN s.learningSpace ls WHERE ls.userId = :userId AND (f.nextReviewDate IS NULL OR f.nextReviewDate <= :now)")
    long countDueFlashcards(@Param("userId") String userId, @Param("now") LocalDateTime now);

    List<Flashcard> findByDeckIdAndNextReviewDateBefore(Long deckId, LocalDateTime now);
}
