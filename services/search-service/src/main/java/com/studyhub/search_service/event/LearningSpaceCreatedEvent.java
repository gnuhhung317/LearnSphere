package com.studyhub.search_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningSpaceCreatedEvent {
    private Long id;
    private String title;
    private String description;
    private String userId;
    private LocalDateTime createdAt;
}
