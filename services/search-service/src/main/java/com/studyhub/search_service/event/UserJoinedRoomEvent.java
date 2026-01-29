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
public class UserJoinedRoomEvent {
    private Long roomId;
    private String roomName;
    private String userId;
    private String username;
    private String role;
    private Instant joinedAt;
    private String eventId;
    private Long timestamp;
}
