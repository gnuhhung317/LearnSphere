package com.studyhub.chat_service.service;

import com.studyhub.chat_service.dto.request.AddReactionRequest;
import com.studyhub.chat_service.dto.request.EditMessageRequest;
import com.studyhub.chat_service.dto.request.SendMessageRequest;
import com.studyhub.chat_service.dto.response.MessageResponse;
import com.studyhub.chat_service.entity.*;
import com.studyhub.chat_service.exception.MessageNotFoundException;
import com.studyhub.chat_service.exception.UnauthorizedException;
import com.studyhub.chat_service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {
    
    private static final int MAX_PINNED_MESSAGES = 5;
    
    private final MessageRepository messageRepository;
    private final MessageAttachmentRepository attachmentRepository;
    private final MessageReactionRepository reactionRepository;
    private final RoomRepository roomRepository;
    
    @Transactional
    public MessageResponse sendMessage(Long roomId, SendMessageRequest request, Long senderId) {
        log.info("Sending message to room: {} by user: {}", roomId, senderId);
        
        // Validate membership
        validateMembership(roomId, senderId);
        
        // Validate parent message if replying
        if (request.getParentMessageId() != null) {
            Message parentMessage = getMessageOrThrow(request.getParentMessageId());
            if (!parentMessage.getRoom().getId().equals(roomId)) {
                throw new IllegalArgumentException("Parent message does not belong to this room");
            }
        }
        
        // Create message
        Message message = Message.builder()
                .room(roomRepository.findById(roomId)
                        .orElseThrow(() -> new IllegalArgumentException("Room not found")))
                .senderId(senderId)
                .content(request.getContent())
                .parentMessageId(request.getParentMessageId())
                .isPinned(false)
                .isEdited(false)
                .isDeleted(false)
                .build();
        
        Message savedMessage = messageRepository.save(message);
        
        // Save attachments
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            List<MessageAttachment> attachments = request.getAttachments().stream()
                    .map(dto -> MessageAttachment.builder()
                            .message(savedMessage)
                            .fileId(dto.getFileId())
                            .fileName(dto.getFileName())
                            .fileType(dto.getFileType())
                            .fileSize(dto.getFileSize())
                            .build())
                    .collect(Collectors.toList());
            
            attachmentRepository.saveAll(attachments);
        }
        
        return toMessageResponse(savedMessage, senderId);
    }
    
    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessageHistory(Long roomId, Long currentUserId, Pageable pageable) {
        log.info("Getting message history for room: {}", roomId);
        
        validateMembership(roomId, currentUserId);
        
        return messageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, pageable)
                .map(message -> toMessageResponse(message, currentUserId));
    }
    
    @Transactional
    public MessageResponse editMessage(Long messageId, EditMessageRequest request, Long currentUserId) {
        log.info("Editing message: {} by user: {}", messageId, currentUserId);
        
        Message message = getMessageOrThrow(messageId);
        
        // Only sender can edit
        if (!message.getSenderId().equals(currentUserId)) {
            throw new UnauthorizedException("Only the sender can edit this message");
        }
        
        if (message.getIsDeleted()) {
            throw new IllegalStateException("Cannot edit deleted message");
        }
        
        message.setContent(request.getContent());
        message.setIsEdited(true);
        
        Message updatedMessage = messageRepository.save(message);
        return toMessageResponse(updatedMessage, currentUserId);
    }
    
    @Transactional
    public void deleteMessage(Long messageId, Long currentUserId) {
        log.info("Deleting message: {} by user: {}", messageId, currentUserId);
        
        Message message = getMessageOrThrow(messageId);
        
        // Sender or room owner can delete
        boolean isSender = message.getSenderId().equals(currentUserId);
        boolean isOwner = roomRepository.isOwnerOfRoom(message.getRoom().getId(), currentUserId);
        
        if (!isSender && !isOwner) {
            throw new UnauthorizedException("Only sender or room owner can delete this message");
        }
        
        message.setIsDeleted(true);
        message.setContent("[Deleted]");
        messageRepository.save(message);
    }
    
    @Transactional
    public void addReaction(Long messageId, AddReactionRequest request, Long currentUserId) {
        log.info("Adding reaction to message: {} by user: {}", messageId, currentUserId);
        
        Message message = getMessageOrThrow(messageId);
        validateMembership(message.getRoom().getId(), currentUserId);
        
        // Check if reaction already exists
        MessageReactionId reactionId = new MessageReactionId(messageId, currentUserId, request.getEmoji());
        if (reactionRepository.existsById(reactionId)) {
            return; // Already reacted
        }
        
        MessageReaction reaction = MessageReaction.builder()
                .id(reactionId)
                .message(message)
                .build();
        
        reactionRepository.save(reaction);
    }
    
    @Transactional
    public void removeReaction(Long messageId, String emoji, Long currentUserId) {
        log.info("Removing reaction from message: {} by user: {}", messageId, currentUserId);
        
        Message message = getMessageOrThrow(messageId);
        validateMembership(message.getRoom().getId(), currentUserId);
        
        MessageReactionId reactionId = new MessageReactionId(messageId, currentUserId, emoji);
        reactionRepository.deleteById(reactionId);
    }
    
    @Transactional
    public MessageResponse pinMessage(Long messageId, Long currentUserId) {
        log.info("Pinning message: {} by user: {}", messageId, currentUserId);
        
        Message message = getMessageOrThrow(messageId);
        Long roomId = message.getRoom().getId();
        
        // Only room owner can pin
        if (!roomRepository.isOwnerOfRoom(roomId, currentUserId)) {
            throw new UnauthorizedException("Only room owner can pin messages");
        }
        
        // Check max pinned messages
        long pinnedCount = messageRepository.findPinnedMessagesByRoomId(roomId).size();
        if (pinnedCount >= MAX_PINNED_MESSAGES) {
            throw new IllegalStateException("Maximum pinned messages reached (5)");
        }
        
        message.setIsPinned(true);
        Message pinnedMessage = messageRepository.save(message);
        
        return toMessageResponse(pinnedMessage, currentUserId);
    }
    
    @Transactional
    public void unpinMessage(Long messageId, Long currentUserId) {
        log.info("Unpinning message: {} by user: {}", messageId, currentUserId);
        
        Message message = getMessageOrThrow(messageId);
        
        // Only room owner can unpin
        if (!roomRepository.isOwnerOfRoom(message.getRoom().getId(), currentUserId)) {
            throw new UnauthorizedException("Only room owner can unpin messages");
        }
        
        message.setIsPinned(false);
        messageRepository.save(message);
    }
    
    @Transactional(readOnly = true)
    public List<MessageResponse> getPinnedMessages(Long roomId, Long currentUserId) {
        log.info("Getting pinned messages for room: {}", roomId);
        
        validateMembership(roomId, currentUserId);
        
        return messageRepository.findPinnedMessagesByRoomId(roomId).stream()
                .map(message -> toMessageResponse(message, currentUserId))
                .collect(Collectors.toList());
    }
    
    // Helper methods
    
    private Message getMessageOrThrow(Long messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException(messageId));
    }
    
    private void validateMembership(Long roomId, Long userId) {
        if (!roomRepository.existsMemberInRoom(roomId, userId)) {
            throw new UnauthorizedException("User is not a member of this room");
        }
    }
    
    private MessageResponse toMessageResponse(Message message, Long currentUserId) {
        // Get attachments
        List<MessageResponse.AttachmentInfo> attachments = 
                attachmentRepository.findByMessageId(message.getId()).stream()
                        .map(att -> new MessageResponse.AttachmentInfo(
                                att.getFileId(),
                                att.getFileName(),
                                att.getFileType(),
                                att.getFileSize()
                        ))
                        .collect(Collectors.toList());
        
        // Get reaction counts
        List<Object[]> reactionCounts = reactionRepository.countReactionsByMessageId(message.getId());
        Map<String, Integer> reactionCountsMap = new HashMap<>();
        for (Object[] result : reactionCounts) {
            String emoji = (String) result[0];
            Long count = (Long) result[1];
            reactionCountsMap.put(emoji, count.intValue());
        }
        
        // Get user's reactions
        List<String> userReactions = reactionRepository.findByIdMessageIdAndIdUserId(
                message.getId(), currentUserId).stream()
                .map(reaction -> reaction.getId().getEmoji())
                .collect(Collectors.toList());
        
        // TODO: Fetch sender details from User Service
        MessageResponse.SenderInfo senderInfo = new MessageResponse.SenderInfo(
                message.getSenderId(),
                "user" + message.getSenderId(), // Placeholder
                "User " + message.getSenderId(), // Placeholder
                null // No avatar URL
        );
        
        return MessageResponse.builder()
                .id(message.getId())
                .roomId(message.getRoom().getId())
                .sender(senderInfo)
                .content(message.getContent())
                .parentMessageId(message.getParentMessageId())
                .isPinned(message.getIsPinned())
                .isEdited(message.getIsEdited())
                .isDeleted(message.getIsDeleted())
                .attachments(attachments)
                .reactionCounts(reactionCountsMap)
                .userReactions(userReactions)
                .createdAt(message.getCreatedAt().toInstant(java.time.ZoneOffset.UTC))
                .updatedAt(message.getUpdatedAt().toInstant(java.time.ZoneOffset.UTC))
                .build();
    }
}
