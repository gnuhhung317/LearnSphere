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
public class ChatMessageCreatedEvent {
    
    private Long messageId;
    private Long roomId;
    private Long channelId;
    private String senderId;
    private String senderUsername;
    private String content;
    private String messageType; // TEXT, IMAGE, FILE, etc.
    private Instant createdAt;
    private String eventId;
    private Long timestamp;
    
    public static ChatMessageCreatedEvent from(
            Long messageId, 
            Long roomId, 
            Long channelId,
            String senderId,
            String senderUsername,
            String content, 
            String messageType,
            Instant createdAt) {
        return ChatMessageCreatedEvent.builder()
                .messageId(messageId)
                .roomId(roomId)
                .channelId(channelId)
                .senderId(senderId)
                .senderUsername(senderUsername)
                .content(content)
                .messageType(messageType)
                .createdAt(createdAt)
                .eventId(java.util.UUID.randomUUID().toString())
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
