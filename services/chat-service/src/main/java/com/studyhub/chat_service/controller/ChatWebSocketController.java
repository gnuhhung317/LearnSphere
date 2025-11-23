package com.studyhub.chat_service.controller;

import com.studyhub.chat_service.dto.request.SendMessageRequest;
import com.studyhub.chat_service.dto.response.MessageResponse;
import com.studyhub.chat_service.dto.response.TypingEvent;
import com.studyhub.chat_service.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Instant;

/**
 * WebSocket controller for real-time chat operations.
 * 
 * Client subscribes to:
 * - /topic/rooms/{roomId} - Receives new messages
 * - /topic/rooms/{roomId}/typing - Receives typing indicators
 * 
 * Client sends to:
 * - /app/rooms/{roomId}/messages - Send new message
 * - /app/rooms/{roomId}/typing - Send typing indicator
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
    
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    
    // TODO: Replace with JWT authentication from WebSocket session
    private static final Long MOCK_USER_ID = 1L;
    
    /**
     * Handle incoming messages via WebSocket.
     * Client sends to: /app/rooms/{roomId}/messages
     * Broadcast to: /topic/rooms/{roomId}
     */
    @MessageMapping("/rooms/{roomId}/messages")
    @SendTo("/topic/rooms/{roomId}")
    public MessageResponse handleMessage(
            @DestinationVariable Long roomId,
            @Valid SendMessageRequest request) {
        
        log.info("WebSocket message received for room: {} from user: {}", roomId, MOCK_USER_ID);
        
        try {
            // Save message to database
            MessageResponse message = messageService.sendMessage(roomId, request, MOCK_USER_ID);
            
            log.info("Message saved and broadcasting to /topic/rooms/{}", roomId);
            return message;
            
        } catch (Exception e) {
            log.error("Error processing WebSocket message: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Handle typing indicators via WebSocket.
     * Client sends to: /app/rooms/{roomId}/typing
     * Broadcast to: /topic/rooms/{roomId}/typing
     */
    @MessageMapping("/rooms/{roomId}/typing")
    public void handleTypingIndicator(
            @DestinationVariable Long roomId,
            TypingEvent typingEvent) {
        
        log.debug("Typing indicator for room: {} from user: {}", roomId, typingEvent.getUserId());
        
        try {
            // Validate membership (optional, can cache this)
            // For now, just broadcast the typing event
            
            // Set server timestamp
            TypingEvent event = TypingEvent.builder()
                    .userId(typingEvent.getUserId())
                    .username(typingEvent.getUsername())
                    .timestamp(Instant.now())
                    .build();
            
            // Broadcast to all subscribers except sender
            messagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/typing", event);
            
        } catch (Exception e) {
            log.error("Error processing typing indicator: {}", e.getMessage());
        }
    }
    
    /**
     * Alternative method: Send message to specific user (private message).
     * Not used in current implementation but useful for notifications.
     */
    public void sendMessageToUser(Long userId, MessageResponse message) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/messages",
                message
        );
    }
}
