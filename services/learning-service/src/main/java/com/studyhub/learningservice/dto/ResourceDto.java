package com.studyhub.learningservice.dto;

import com.studyhub.learningservice.domain.ResourceType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResourceDto {
    private Long id;
    private String title;
    private ResourceType type;
    private String url;
    private String description;
    private Integer orderIndex;
    private boolean isCompleted;
    private String status;
}
