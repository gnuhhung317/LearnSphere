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
public class UserJoinedRoomEvent {
    
    private Long roomId;
    private String userId;
    private String username;
    private String role;
    private Instant joinedAt;
    private String eventId;
    private Long timestamp;
    
    public static UserJoinedRoomEvent from(
            Long roomId,
            String userId,
            String username,
            String role,
            Instant joinedAt) {
        return UserJoinedRoomEvent.builder()
                .roomId(roomId)
                .userId(userId)
                .username(username)
                .role(role)
                .joinedAt(joinedAt)
                .eventId(java.util.UUID.randomUUID().toString())
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
