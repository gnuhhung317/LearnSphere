package com.studyhub.chat_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

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

    public static RoomCreatedEvent from(
            Long roomId,
            String roomName,
            String description,
            String creatorId,
            String roomType,
            Boolean isPublic,
            Instant createdAt) {
        return RoomCreatedEvent.builder()
                .roomId(roomId)
                .roomName(roomName)
                .description(description)
                .creatorId(creatorId)
                .roomType(roomType)
                .isPublic(isPublic)
                .createdAt(createdAt)
                .eventId(java.util.UUID.randomUUID().toString())
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
