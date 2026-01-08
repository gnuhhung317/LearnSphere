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

    @KafkaListener(topics = "chat.files.synced", groupId = "chat-service-ws-group")
    public void handleFileSynced(FileSyncedEvent event) {
        log.info("WebSocket forwarding FileSyncedEvent: {}", event.getFileId());
        
        // Forward to room topic
        messagingTemplate.convertAndSend("/topic/room." + event.getRoomId(), event);
    }
}
