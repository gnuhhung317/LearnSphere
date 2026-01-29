package com.studyhub.ai_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class GeneratedCoursePlan {
    private String title;
    private String description;
    private List<Section> sections;

    @Data
    @NoArgsConstructor
    public static class Section {
        private String title;
        private List<Resource> resources;
    }

    @Data
    @NoArgsConstructor
    public static class Resource {
        private String title;
        private String type; // FILE, LINK, NOTE
        private String url; // Content
        private String description;
    }
}
