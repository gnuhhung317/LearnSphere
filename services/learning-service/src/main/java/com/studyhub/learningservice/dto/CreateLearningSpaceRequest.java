package com.studyhub.learningservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateLearningSpaceRequest {

    @NotBlank(message = "Title is required")
    private String title;
    private String description;
}
