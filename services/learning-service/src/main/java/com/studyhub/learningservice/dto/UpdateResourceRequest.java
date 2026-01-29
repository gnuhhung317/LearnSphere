package com.studyhub.learningservice.dto;

import lombok.Data;

@Data
public class UpdateResourceRequest {
    private String title;
    private String description;
    private Boolean isCompleted;
    private String status;
}
