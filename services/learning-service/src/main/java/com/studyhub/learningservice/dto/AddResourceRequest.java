package com.studyhub.learningservice.dto;

import com.studyhub.learningservice.domain.ResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AddResourceRequest {

    // If provided, add to this section. If null, might create a default one or
    // throw error.
    private Long sectionId;

    private String title;

    @NotNull(message = "Type is required")
    private ResourceType type;

    private String url;

    private String description;
}
