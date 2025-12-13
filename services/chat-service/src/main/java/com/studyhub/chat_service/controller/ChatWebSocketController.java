package com.studyhub.chat_service.controller;

import com.studyhub.chat_service.dto.request.SendMessageRequest;
import com.studyhub.chat_service.dto.response.MessageResponse;
import com.studyhub.chat_service.dto.response.TypingEvent;
import com.studyhub.chat_service.service.MessageService;
import com.studyhub.chat_service.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.Instant;

/**
 * WebSocket controller for real-time chat operations.
 *
 * Client subscribes to: - /topic/rooms/{roomId} - Receives new messages and
 * message updates (edit/delete/reactions) - /topic/rooms/{roomId}/typing -
 * Receives typing indicators - /topic/rooms/{roomId}/members - Receives member
 * join/leave/removed events
 *
 * Client sends to: - /app/rooms/{roomId}/messages - Send new message -
 * /app/rooms/{roomId}/typing - Send typing indicator
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle incoming messages via WebSocket. Client sends to:
     * /app/rooms/{roomId}/channels/{channelId}/messages 
     * Broadcast to: /topic/rooms/{roomId}/channels/{channelId}
     */
    @MessageMapping("/rooms/{roomId}/channels/{channelId}/messages")
    public void handleMessage(
            @DestinationVariable Long roomId,
            @DestinationVariable Long channelId,
            @Valid SendMessageRequest request,
            Principal principal) {

        // Extract JWT from Principal (set by WebSocketAuthInterceptor)
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;
        Jwt jwt = (Jwt) auth.getPrincipal();
        Long userId = JwtUtil.getUserIdFromJwt(jwt);
        
        log.info("WebSocket message received for channel: {} in room: {} from user: {}", channelId, roomId, userId);

        try {
            // Save message to database
            MessageResponse message = messageService.sendMessage(roomId, channelId, request, userId);
            log.info("Message saved and broadcasted to /topic/rooms/{}/channels/{}", roomId, channelId);
            // No explicit return: broadcasting is handled in MessageService to avoid double-send

        } catch (Exception e) {
            log.error("Error processing WebSocket message: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Handle typing indicators via WebSocket. Client sends to:
     * /app/rooms/{roomId}/channels/{channelId}/typing 
     * Broadcast to: /topic/rooms/{roomId}/channels/{channelId}/typing
     */
    @MessageMapping("/rooms/{roomId}/channels/{channelId}/typing")
    public void handleTypingIndicator(
            @DestinationVariable Long roomId,
            @DestinationVariable Long channelId,
            TypingEvent typingEvent,
            Principal principal) {

        // Extract JWT from Principal
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;
        Jwt jwt = (Jwt) auth.getPrincipal();
        Long userId = JwtUtil.getUserIdFromJwt(jwt);

        log.debug("Typing indicator for channel: {} in room: {} from user: {}", channelId, roomId, userId);

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
            messagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/channels/" + channelId + "/typing", event);

        } catch (Exception e) {
            log.error("Error processing typing indicator: {}", e.getMessage());
        }
    }

    /**
     * Alternative method: Send message to specific user (private message). Not
     * used in current implementation but useful for notifications.
     */
    public void sendMessageToUser(Long userId, MessageResponse message) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/messages",
                message
        );
    }
}
