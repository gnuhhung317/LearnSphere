package com.studyhub.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for learning path information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathDto {

    private Long id;
    private String name;
    private String description;
    private Integer progress;
    private String color;
    private Boolean isActive;
}
