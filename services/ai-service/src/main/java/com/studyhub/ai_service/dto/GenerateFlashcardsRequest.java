package com.studyhub.ai_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateFlashcardsRequest {
    private String content;
    private String topic;
    private int count = 10;
    private Long resourceId;
    private String resourceType;
    private String resourceUrl;
}
