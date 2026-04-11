package com.studyhub.search_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomCreatedEvent {
    private Long roomId;
    private String roomName;
    private String description;
    private String creatorId;
    private String roomType;
    private Boolean isPublic;
    private Instant createdAt;
    private String eventId;
    private Long timestamp;
}
