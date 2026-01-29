package com.studyhub.ai_service.listener;

import com.studyhub.ai_service.event.ResourceAddedEvent;
import com.studyhub.ai_service.service.FileProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LearningEventListener {

    private final FileProcessingService fileProcessingService;

    @KafkaListener(topics = "learning.resource.added", groupId = "ai-service-learning-group", properties = {
            "spring.json.value.default.type=com.studyhub.ai_service.event.ResourceAddedEvent"
    })
    public void handleResourceAdded(ResourceAddedEvent event) {
        log.info("Received ResourceAddedEvent: {}", event);
        // Process asynchronously
        new Thread(() -> fileProcessingService.processResource(event)).start();
    }

    @KafkaListener(topics = "learning.resource.deleted", groupId = "ai-service-learning-group", properties = {
            "spring.json.value.default.type=com.studyhub.ai_service.event.ResourceDeletedEvent"
    })
    public void handleResourceDeleted(com.studyhub.ai_service.event.ResourceDeletedEvent event) {
        log.info("Received ResourceDeletedEvent: {}", event);
        // Process asynchronously
        new Thread(() -> fileProcessingService.deleteResourceEmbeddings(event)).start();
    }

    @KafkaListener(topics = "learning.space.deleted", groupId = "ai-service-learning-group", properties = {
            "spring.json.value.default.type=com.studyhub.ai_service.event.LearningSpaceDeletedEvent"
    })
    public void handleLearningSpaceDeleted(com.studyhub.ai_service.event.LearningSpaceDeletedEvent event) {
        log.info("Received LearningSpaceDeletedEvent: {}", event);
        // Process asynchronously
        new Thread(() -> fileProcessingService.deleteLearningSpaceEmbeddings(event)).start();
    }
}
