package com.studyhub.ai_service.listener;

import com.studyhub.ai_service.event.FileSyncedEvent;
import com.studyhub.ai_service.service.FileProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatEventListener {

    private final FileProcessingService fileProcessingService;

    @KafkaListener(topics = "chat.files.synced", groupId = "ai-service-group")
    public void handleFileSynced(FileSyncedEvent event) {
        log.info("Received FileSyncedEvent: {}", event);
        // Process asynchronously
        new Thread(() -> fileProcessingService.processFile(event)).start();
    }
}
