package com.studyhub.learningservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class LearningSpaceDto {

    private Long id;
    private String title;
    private String description;
    private String userId;
    private List<SectionDto> sections;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
