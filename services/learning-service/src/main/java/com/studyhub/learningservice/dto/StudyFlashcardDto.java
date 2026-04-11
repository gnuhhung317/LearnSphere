package com.studyhub.learningservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Extended flashcard DTO for study mode.
 * Includes context about which deck/space the card belongs to.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyFlashcardDto {
    private Long id;
    private String front;
    private String back;

    // Context info
    private Long deckId;
    private String deckTitle;
    private Long learningSpaceId;
    private String learningSpaceTitle;

    // SRS info (for advanced display)
    private Integer repetitions;
    private Integer intervalDays;
}
