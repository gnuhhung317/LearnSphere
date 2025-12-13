package com.studyhub.chat_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomSummary {
    
    private Long id;
    private String name;
    private String description;
    private Integer memberCount;
    private Boolean isOwner;
    private Boolean isMember;
    private LocalDateTime lastActivity;
    private LocalDateTime createdAt;
}
