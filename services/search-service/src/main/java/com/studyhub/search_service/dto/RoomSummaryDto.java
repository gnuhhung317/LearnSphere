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
public class RoomSummaryDto {
    private Long id;
    private String name;
    private int memberCount;
    private LocalDateTime createdAt;
    private LocalDateTime lastActivity;
}
