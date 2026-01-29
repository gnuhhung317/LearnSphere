package com.studyhub.chat_service.listener;

import com.studyhub.chat_service.event.FileSyncedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    private final com.studyhub.chat_service.service.OnlineUserService onlineUserService;

    @KafkaListener(topics = "chat.files.synced", groupId = "chat-service-ws-group")
    public void handleFileSynced(FileSyncedEvent event) {
        log.info("WebSocket forwarding FileSyncedEvent: {}", event.getFileId());

        // Forward to room topic
        messagingTemplate.convertAndSend("/topic/room." + event.getRoomId(), event);
    }

    @org.springframework.context.event.EventListener
    public void handleWebSocketConnectListener(org.springframework.web.socket.messaging.SessionConnectedEvent event) {
        org.springframework.messaging.simp.stomp.StompHeaderAccessor headerAccessor = org.springframework.messaging.simp.stomp.StompHeaderAccessor
                .wrap(event.getMessage());
        try {
            if (headerAccessor.getUser() != null) {
                String userId = headerAccessor.getUser().getName();
                String sessionId = headerAccessor.getSessionId();
                log.info("Received new web socket connection. User: {}, Session: {}", userId, sessionId);
                onlineUserService.addUser(userId, sessionId);
            }
        } catch (Exception e) {
            log.warn("Failed to track online user from connect event: {}", e.getMessage());
        }
    }

    @org.springframework.context.event.EventListener
    public void handleWebSocketDisconnectListener(
            org.springframework.web.socket.messaging.SessionDisconnectEvent event) {
        org.springframework.messaging.simp.stomp.StompHeaderAccessor headerAccessor = org.springframework.messaging.simp.stomp.StompHeaderAccessor
                .wrap(event.getMessage());
        try {
            String userId = null;
            if (headerAccessor.getUser() != null) {
                userId = headerAccessor.getUser().getName();
            }
            String sessionId = headerAccessor.getSessionId();

            if (userId != null) {
                log.info("Web socket disconnected. User: {}, Session: {}", userId, sessionId);
                onlineUserService.removeUser(userId, sessionId);
            }
        } catch (Exception e) {
            log.warn("Failed to track online user from disconnect event: {}", e.getMessage());
        }
    }
}
