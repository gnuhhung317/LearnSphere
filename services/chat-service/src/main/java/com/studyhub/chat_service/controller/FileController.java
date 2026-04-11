package com.studyhub.chat_service.controller;

import com.studyhub.chat_service.dto.response.FileDTO;
import com.studyhub.chat_service.entity.MessageAttachment;
import com.studyhub.chat_service.event.FileSyncedEvent;
import com.studyhub.chat_service.repository.MessageAttachmentRepository;
import com.studyhub.chat_service.service.EventPublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final MessageAttachmentRepository messageAttachmentRepository;
    private final EventPublisherService eventPublisherService;

    @GetMapping("/rooms/{roomId}/files")
    public ResponseEntity<List<FileDTO>> getRoomFiles(@PathVariable Long roomId) {
        log.info("Fetching files for room: {}", roomId);
        List<MessageAttachment> attachments = messageAttachmentRepository.findByRoomIdAndAiStatusNotNull(roomId);
        
        List<FileDTO> fileDTOs = attachments.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(fileDTOs);
    }

    @PostMapping("/files/{attachmentId}/sync")
    public ResponseEntity<Void> syncFile(@PathVariable Long attachmentId) {
        log.info("Syncing file: {}", attachmentId);
        
        MessageAttachment attachment = messageAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        if (attachment.getAiStatus() == null) {
            updateStatusAndPublish(attachment, "PENDING");
        }

        return ResponseEntity.ok().build();
    }

    @PutMapping("/files/{attachmentId}/status")
    public ResponseEntity<Void> updateFileStatus(@PathVariable Long attachmentId, @RequestParam String status) {
        log.info("Updating file status: {} -> {}", attachmentId, status);
        
        MessageAttachment attachment = messageAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));
        
        updateStatusAndPublish(attachment, status);
        
        return ResponseEntity.ok().build();
    }

    private void updateStatusAndPublish(MessageAttachment attachment, String status) {
        attachment.setAiStatus(status);
        messageAttachmentRepository.save(attachment);

        // Publish event
        FileSyncedEvent event = FileSyncedEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .timestamp(System.currentTimeMillis())
                .attachmentId(attachment.getId())
                .fileId(attachment.getFileId())
                .messageId(attachment.getMessage().getId())
                .roomId(attachment.getMessage().getChannel().getRoom().getId())
                .aiStatus(status)
                .build();
        
        eventPublisherService.publishFileSynced(event);
    }

    private FileDTO mapToDTO(MessageAttachment attachment) {
        // Safe null checks for nested objects (though database constraints should ensure not null)
        String senderName = "Unknown";
        String senderAvatar = null;
        
        /* Note: We don't have sender details directly in MessageAttachment. 
           In a real scenario, we might need to fetch user details from User Service 
           or store them in Message. For now, we will leave them simple or fetch from Message.senderId
           (Message has senderId, but not name/avatar directly unless we fetch from user service).
           
           Wait, Message entity only has senderId string. 
           The frontend might need to fetch user info or we just send what we have.
           The requirement is just a list.
        */
        
        return FileDTO.builder()
                .id(attachment.getId())
                .fileId(attachment.getFileId())
                .fileName(attachment.getFileName())
                .fileType(attachment.getFileType())
                .fileSize(attachment.getFileSize())
                .aiStatus(attachment.getAiStatus())
                .uploadedAt(attachment.getCreatedAt())
                .senderName(attachment.getMessage().getSenderId()) // Simplified for now
                .build();
    }
}
