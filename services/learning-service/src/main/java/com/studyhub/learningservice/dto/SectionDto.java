package com.studyhub.learningservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SectionDto {
    private Long id;
    private String title;
    private Integer orderIndex;
    private List<ResourceDto> resources;
}
