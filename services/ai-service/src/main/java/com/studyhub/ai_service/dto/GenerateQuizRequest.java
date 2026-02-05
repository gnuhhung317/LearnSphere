package com.studyhub.ai_service.dto;

import lombok.Data;

@Data
public class GenerateQuizRequest {
    private String content; // Text content to generate quiz from
    private String topic;
    private int numberOfQuestions = 15;
    private String difficulty = "Medium";
    private Long resourceId;
    private String resourceType;
    private String resourceUrl;
}
