package com.studyhub.chat_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDeletedEvent {

    private Long roomId;
    private String name;
    private String creatorId;
    private Instant deletedAt;
    private String eventId;
    private Long timestamp;

    public static RoomDeletedEvent from(Long roomId, String name, String creatorId) {
        return RoomDeletedEvent.builder()
                .roomId(roomId)
                .name(name)
                .creatorId(creatorId)
                .deletedAt(Instant.now())
                .eventId(java.util.UUID.randomUUID().toString())
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
