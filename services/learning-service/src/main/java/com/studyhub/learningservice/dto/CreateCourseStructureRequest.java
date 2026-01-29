package com.studyhub.learningservice.dto;

import com.studyhub.learningservice.domain.ResourceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CreateCourseStructureRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @Valid
    private List<SectionRequest> sections;

    @Data
    @NoArgsConstructor
    public static class SectionRequest {
        @NotBlank(message = "Section title is required")
        private String title;

        @Valid
        private List<ResourceRequest> resources;
    }

    @Data
    @NoArgsConstructor
    public static class ResourceRequest {
        @NotBlank(message = "Resource title is required")
        private String title;

        @NotNull(message = "Type is required")
        private ResourceType type;

        private String url; // Can be content for notes

        private String description;
    }
}
