package com.studyhub.ai_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GeneratedCoursePlanRequest {
    private String topic;
    private String goal;
    private String level; // Beginner, Intermediate, Advanced
}
