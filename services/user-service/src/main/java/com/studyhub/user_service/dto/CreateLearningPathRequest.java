package com.studyhub.user_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new learning path
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLearningPathRequest {

    
    @NotBlank(message = "Learning path name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
       private String name;

       @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

       @Min(value = 0, message = "Progress must be at least 0")
    @Max(value = 100, message = "Progress must not exceed 100")
    private Integer progress = 0;

    @Size(max = 50, message = "Color must not exceed 50 characters")
    private String color;
}
