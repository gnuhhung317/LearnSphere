package com.studyhub.ai_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class GeneratedQuizResponse {
    private String title;
    private String description;
    private List<GeneratedQuestion> questions;

    @Data
    public static class GeneratedQuestion {
        private String text;
        private String type; // MULTIPLE_CHOICE, TRUE_FALSE, OPEN_ENDED
        private List<String> options;
        private String correctAnswer;
        private String explanation;
    }
}
