package com.studyhub.chat_service.service;

import com.studyhub.chat_service.event.ChatMessageCreatedEvent;
import com.studyhub.chat_service.event.FileSyncedEvent;
import com.studyhub.chat_service.event.RoomCreatedEvent;
import com.studyhub.chat_service.event.UserJoinedRoomEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EventPublisherService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EventPublisherService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publish ChatMessageCreated event to Kafka
     */
    public void publishMessageCreated(ChatMessageCreatedEvent event) {
        try {
            kafkaTemplate.send("chat.messages.created", 
                    event.getMessageId().toString(), 
                    event);
            log.info("Published ChatMessageCreated event: messageId={}, roomId={}", 
                    event.getMessageId(), event.getRoomId());
        } catch (Exception e) {
            log.error("Failed to publish ChatMessageCreated event: messageId={}", 
                    event.getMessageId(), e);
        }
    }

    /**
     * Publish RoomCreated event to Kafka
     */
    public void publishRoomCreated(RoomCreatedEvent event) {
        try {
            kafkaTemplate.send("chat.rooms.created", 
                    event.getRoomId().toString(), 
                    event);
            log.info("Published RoomCreated event: roomId={}, roomName={}", 
                    event.getRoomId(), event.getRoomName());
        } catch (Exception e) {
            log.error("Failed to publish RoomCreated event: roomId={}", 
                    event.getRoomId(), e);
        }
    }

    /**
     * Publish UserJoinedRoom event to Kafka
     */
    public void publishUserJoinedRoom(UserJoinedRoomEvent event) {
        try {
            kafkaTemplate.send("chat.rooms.user-joined", 
                    event.getRoomId().toString(), 
                    event);
            log.info("Published UserJoinedRoom event: roomId={}, userId={}", 
                    event.getRoomId(), event.getUserId());
        } catch (Exception e) {
            log.error("Failed to publish UserJoinedRoom event: roomId={}, userId={}", 
                    event.getRoomId(), event.getUserId(), e);
        }
    }

    /**
     * Publish message edited event
     */
    public void publishMessageEdited(Long messageId, Long roomId, String newContent) {
        try {
            var event = ChatMessageCreatedEvent.builder()
                    .messageId(messageId)
                    .roomId(roomId)
                    .content(newContent)
                    .eventId(java.util.UUID.randomUUID().toString())
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            kafkaTemplate.send("chat.messages.edited", messageId.toString(), event);
            log.info("Published MessageEdited event: messageId={}", messageId);
        } catch (Exception e) {
            log.error("Failed to publish MessageEdited event: messageId={}", messageId, e);
        }
    }

    /**
     * Publish message deleted event
     */
    public void publishMessageDeleted(Long messageId, Long roomId) {
        try {
            var event = ChatMessageCreatedEvent.builder()
                    .messageId(messageId)
                    .roomId(roomId)
                    .eventId(java.util.UUID.randomUUID().toString())
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            kafkaTemplate.send("chat.messages.deleted", messageId.toString(), event);
            log.info("Published MessageDeleted event: messageId={}", messageId);
        } catch (Exception e) {
            log.error("Failed to publish MessageDeleted event: messageId={}", messageId, e);
        }
    }

    /**
     * Publish file synced event
     */
    public void publishFileSynced(FileSyncedEvent event) {
        try {
            kafkaTemplate.send("chat.files.synced", event.getFileId(), event);
            log.info("Published FileSynced event: fileId={}", event.getFileId());
        } catch (Exception e) {
            log.error("Failed to publish FileSynced event: fileId={}", event.getFileId(), e);
        }
    }
}
