package com.studyhub.ai_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class GeneratedFlashcardsResponse {
    private String title;
    private List<GeneratedFlashcard> flashcards;

    @Data
    public static class GeneratedFlashcard {
        private String front;
        private String back;
    }
}
