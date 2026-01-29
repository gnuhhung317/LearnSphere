package com.studyhub.search_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityItemDto {
    private String id;
    private ActivityType type;
    private String title;
    private String description;
    private LocalDateTime timestamp;
    private String link;
    private String metadata;

    public enum ActivityType {
        COURSE,
        FILE,
        ROOM_JOINED,
        UNKNOWN
    }
}
