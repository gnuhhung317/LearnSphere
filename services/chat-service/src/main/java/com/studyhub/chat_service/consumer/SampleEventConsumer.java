package com.studyhub.chat_service.consumer;

import com.studyhub.chat_service.event.ChatMessageCreatedEvent;
import com.studyhub.chat_service.event.RoomCreatedEvent;
import com.studyhub.chat_service.event.UserJoinedRoomEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Sample Kafka consumer to demonstrate event consumption
 * In production, this would be implemented in other services (Search, AI, etc.)
 */
@Slf4j
@Component
public class SampleEventConsumer {

    /**
     * Example: Search Service would consume this to index messages
     */
    @KafkaListener(
            topics = "chat.messages.created",
            groupId = "chat-service-demo-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleMessageCreated(ChatMessageCreatedEvent event) {
        log.info("📨 Consumed ChatMessageCreated event: messageId={}, roomId={}, channelId={}, content='{}'",
                event.getMessageId(),
                event.getRoomId(),
                event.getChannelId(),
                event.getContent());

        // TODO: In Search Service, this would trigger Elasticsearch indexing
        // searchService.indexMessage(event);
    }

    /**
     * Example: Notification Service would consume this to send notifications
     */
    @KafkaListener(
            topics = "chat.rooms.created",
            groupId = "chat-service-demo-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleRoomCreated(RoomCreatedEvent event) {
        log.info("🏠 Consumed RoomCreated event: roomId={}, roomName='{}', creatorId={}",
                event.getRoomId(),
                event.getRoomName(),
                event.getCreatorId());

        // TODO: In Analytics Service, this would track room creation metrics
        // analyticsService.trackRoomCreation(event);
    }

    /**
     * Example: User Service would consume this to update user stats
     */
    @KafkaListener(
            topics = "chat.rooms.user-joined",
            groupId = "chat-service-demo-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleUserJoinedRoom(UserJoinedRoomEvent event) {
        log.info("👤 Consumed UserJoinedRoom event: userId={}, roomId={}, username='{}'",
                event.getUserId(),
                event.getRoomId(),
                event.getUsername());

        // TODO: In User Service, this would update user activity stats
        // userService.incrementRoomJoinCount(event.getUserId());
    }

    /**
     * Example: Audit Service would consume edited messages
     */
    @KafkaListener(
            topics = "chat.messages.edited",
            groupId = "chat-service-demo-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleMessageEdited(ChatMessageCreatedEvent event) {
        log.info("✏️ Consumed MessageEdited event: messageId={}, newContent='{}'",
                event.getMessageId(),
                event.getContent());

        // TODO: In Audit Service, log message edits for compliance
        // auditService.logMessageEdit(event);
    }

    /**
     * Example: Search Service would consume this to remove from index
     */
    @KafkaListener(
            topics = "chat.messages.deleted",
            groupId = "chat-service-demo-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleMessageDeleted(ChatMessageCreatedEvent event) {
        log.info("🗑️ Consumed MessageDeleted event: messageId={}, roomId={}",
                event.getMessageId(),
                event.getRoomId());

        // TODO: In Search Service, remove message from Elasticsearch index
        // searchService.removeMessage(event.getMessageId());
    }
}
