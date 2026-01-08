package com.studyhub.chat_service.service;

import com.studyhub.chat_service.client.UserClient;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final ChannelRepository channelRepository;
    private final RoomRepository roomRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserClient userClient;
    private final EventPublisherService eventPublisher;
    private final com.studyhub.chat_service.client.AiClient aiClient;

    @Transactional
    public MessageResponse sendMessage(Long roomId, Long channelId, SendMessageRequest request, String senderId,
            org.springframework.security.oauth2.jwt.Jwt jwt) {
        log.info("Sending message to channel: {} in room: {} by user: {}", channelId, roomId, senderId);

        // Validate at least content or attachments exist
        boolean hasContent = request.getContent() != null && !request.getContent().trim().isEmpty();
        boolean hasAttachments = request.getAttachments() != null && !request.getAttachments().isEmpty();

        if (!hasContent && !hasAttachments) {
            throw new IllegalArgumentException("Message must have either content or attachments");
        }

        // Validate membership
        validateMembership(roomId, senderId);

        // Validate channel belongs to room
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found"));
        if (!channel.getRoom().getId().equals(roomId)) {
            throw new IllegalArgumentException("Channel does not belong to this room");
        }

        // Validate parent message if replying
        if (request.getParentMessageId() != null) {
            Message parentMessage = getMessageOrThrow(request.getParentMessageId());
            if (!parentMessage.getChannel().getId().equals(channelId)) {
                throw new IllegalArgumentException("Parent message does not belong to this channel");
            }
        }

        // Create message
        Message message = Message.builder()
                .channel(channel)
                .senderId(senderId)
                .content(request.getContent() != null ? request.getContent() : "")
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

        // Set JWT in SecurityContext for User Service calls
        org.springframework.security.core.context.SecurityContextHolder.getContext()
                .setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        jwt, null, java.util.Collections.emptyList()));

        MessageResponse response = toMessageResponse(savedMessage, senderId);

        // Publish Kafka event
        try {
            var event = com.studyhub.chat_service.event.ChatMessageCreatedEvent.from(
                    savedMessage.getId(),
                    roomId,
                    channelId,
                    senderId,
                    "user_" + senderId, // TODO: Get actual username from UserClient
                    savedMessage.getContent(),
                    hasAttachments ? "WITH_ATTACHMENTS" : "TEXT",
                    savedMessage.getCreatedAt());
            eventPublisher.publishMessageCreated(event);
        } catch (Exception e) {
            log.error("Failed to publish ChatMessageCreated event: {}", e.getMessage(), e);
        }

        // Broadcast the new message to websocket subscribers of the channel
        try {
            messagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/channels/" + channelId, response);
        } catch (Exception e) {
            log.error("Failed to broadcast message to WebSocket: {}", e.getMessage(), e);
        }

        // Trigger AI Bot if mentioned
        if (hasContent && request.getContent().contains("@bot")) {
            triggerAiResponse(roomId, channelId, request.getContent(), jwt);
        }

        return response;
    }

    private void triggerAiResponse(Long roomId, Long channelId, String query,
            org.springframework.security.oauth2.jwt.Jwt jwt) {
        log.info("Triggering AI response for room: {}", roomId);

        // Use a simple worker thread or common pool to avoid blocking
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                // Remove @bot from query
                String cleanQuery = query.replace("@bot", "").trim();

                // Set JWT for the Feign call
                org.springframework.security.core.context.SecurityContextHolder.getContext()
                        .setAuthentication(
                                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                        jwt, null, java.util.Collections.emptyList()));

                Map<String, String> aiResponse = aiClient.chat(Map.of(
                        "roomId", roomId,
                        "query", cleanQuery));

                String content = aiResponse.getOrDefault("response", "Sorry, I couldn't generate a response.");

                // Save bot message
                Message botMessage = Message.builder()
                        .channel(channelRepository.getReferenceById(channelId))
                        .senderId("ai-bot")
                        .content(content)
                        .isPinned(false)
                        .isEdited(false)
                        .isDeleted(false)
                        .build();

                Message savedBotMessage = messageRepository.save(botMessage);
                MessageResponse botResponse = toMessageResponse(savedBotMessage, "ai-bot");

                // Broadcast bot message
                messagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/channels/" + channelId, botResponse);

            } catch (Exception e) {
                log.error("Error generating AI response", e);
            }
        });
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessageHistory(Long roomId, String currentUserId, Pageable pageable) {
        log.info("Getting message history for room: {}", roomId);

        validateMembership(roomId, currentUserId);

        Page<Message> messages = messageRepository.findByChannelIdOrderByCreatedAtDesc(roomId, pageable);
        return mapMessagesToResponses(messages, currentUserId);
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessageHistoryByChannel(Long channelId, String currentUserId, Pageable pageable) {
        log.info("Getting message history for channel: {}", channelId);

        // Validate channel exists and user is member of the room
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found"));

        validateMembership(channel.getRoom().getId(), currentUserId);

        Page<Message> messages = messageRepository.findByChannelIdOrderByCreatedAtDesc(channelId, pageable);
        return mapMessagesToResponses(messages, currentUserId);
    }

    @Transactional
    public MessageResponse editMessage(Long messageId, EditMessageRequest request, String currentUserId) {
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
        MessageResponse response = toMessageResponse(updatedMessage, currentUserId);

        // Publish Kafka event for edited message
        try {
            eventPublisher.publishMessageEdited(messageId, response.getRoomId(), request.getContent());
        } catch (Exception e) {
            log.error("Failed to publish MessageEdited event: {}", e.getMessage(), e);
        }

        // Broadcast edited message
        try {
            messagingTemplate.convertAndSend("/topic/rooms/" + response.getRoomId(), response);
        } catch (Exception e) {
            log.error("Failed to broadcast edited message: {}", e.getMessage(), e);
        }

        return response;
    }

    @Transactional
    public void deleteMessage(Long messageId, String currentUserId) {
        log.info("Deleting message: {} by user: {}", messageId, currentUserId);

        Message message = getMessageOrThrow(messageId);

        // Sender or room owner can delete
        boolean isSender = message.getSenderId().equals(currentUserId);
        // Note: roomRepository.isOwnerOfRoom check might be expensive if not optimized,
        // but staying with current logic for now.
        boolean isOwner = roomRepository.isOwnerOfRoom(message.getChannel().getRoom().getId(), currentUserId);

        message.setIsDeleted(true);
        message.setContent("[Deleted]");
        Message deleted = messageRepository.save(message);

        // Broadcast deleted message (message will contain deleted flag and content
        // change)
        try {
            MessageResponse response = toMessageResponse(deleted, currentUserId);
            messagingTemplate.convertAndSend("/topic/rooms/" + response.getRoomId(), response);
        } catch (Exception e) {
            log.error("Failed to broadcast deleted message: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public void addReaction(Long messageId, AddReactionRequest request, String currentUserId) {
        log.info("Adding reaction to message: {} by user: {}", messageId, currentUserId);

        Message message = getMessageOrThrow(messageId);
        validateMembership(message.getChannel().getRoom().getId(), currentUserId);

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

        // Broadcast reaction update to subscribers
        try {
            MessageResponse response = toMessageResponse(message, currentUserId);
            messagingTemplate.convertAndSend("/topic/rooms/" + response.getRoomId(), response);
        } catch (Exception e) {
            log.error("Failed to broadcast reaction added: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public void removeReaction(Long messageId, String emoji, String currentUserId) {
        log.info("Removing reaction from message: {} by user: {}", messageId, currentUserId);

        Message message = getMessageOrThrow(messageId);
        validateMembership(message.getChannel().getRoom().getId(), currentUserId);

        MessageReactionId reactionId = new MessageReactionId(messageId, currentUserId, emoji);
        reactionRepository.deleteById(reactionId);

        // Broadcast reaction update to subscribers
        try {
            MessageResponse response = toMessageResponse(message, currentUserId);
            messagingTemplate.convertAndSend("/topic/rooms/" + response.getRoomId(), response);
        } catch (Exception e) {
            log.error("Failed to broadcast reaction removed: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public MessageResponse pinMessage(Long messageId, String currentUserId) {
        log.info("Pinning message: {} by user: {}", messageId, currentUserId);

        Message message = getMessageOrThrow(messageId);
        Long roomId = message.getChannel().getRoom().getId();

        // Only room owner can pin
        if (!roomRepository.isOwnerOfRoom(roomId, currentUserId)) {
            throw new UnauthorizedException("Only room owner can pin messages");
        }

        // Check max pinned messages
        long pinnedCount = messageRepository.findPinnedMessagesByChannelId(roomId).size();
        if (pinnedCount >= MAX_PINNED_MESSAGES) {
            throw new IllegalStateException("Maximum pinned messages reached (5)");
        }

        message.setIsPinned(true);
        Message pinnedMessage = messageRepository.save(message);
        MessageResponse response = toMessageResponse(pinnedMessage, currentUserId);

        // Broadcast pinned message to subscribers
        try {
            messagingTemplate.convertAndSend("/topic/rooms/" + response.getRoomId(), response);
        } catch (Exception e) {
            log.error("Failed to broadcast pinned message: {}", e.getMessage(), e);
        }

        return response;
    }

    @Transactional
    public void unpinMessage(Long messageId, String currentUserId) {
        log.info("Unpinning message: {} by user: {}", messageId, currentUserId);

        Message message = getMessageOrThrow(messageId);

        // Only room owner can unpin
        if (!roomRepository.isOwnerOfRoom(message.getChannel().getRoom().getId(), currentUserId)) {
            throw new UnauthorizedException("Only room owner can unpin messages");
        }

        message.setIsPinned(false);
        Message updated = messageRepository.save(message);

        // Broadcast unpinned message
        try {
            MessageResponse response = toMessageResponse(updated, currentUserId);
            messagingTemplate.convertAndSend("/topic/rooms/" + response.getRoomId(), response);
        } catch (Exception e) {
            log.error("Failed to broadcast unpinned message: {}", e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getPinnedMessages(Long roomId, String currentUserId) {
        log.info("Getting pinned messages for room: {}", roomId);

        validateMembership(roomId, currentUserId);

        List<Message> messages = messageRepository.findPinnedMessagesByChannelId(roomId);
        return mapMessagesToListResponses(messages, currentUserId);
    }

    // Thread/Reply support
    @Transactional(readOnly = true)
    public List<MessageResponse> getThreadReplies(Long messageId, String currentUserId) {
        log.info("Getting thread replies for message: {}", messageId);

        Message parentMessage = getMessageOrThrow(messageId);
        validateMembership(parentMessage.getChannel().getRoom().getId(), currentUserId);

        List<Message> messages = messageRepository.findRepliesByParentMessageId(messageId);
        return mapMessagesToListResponses(messages, currentUserId);
    }

    @Transactional(readOnly = true)
    public long getReplyCount(Long messageId) {
        return messageRepository.countRepliesByParentMessageId(messageId);
    }

    // Helper methods
    private Message getMessageOrThrow(Long messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException(messageId));
    }

    private void validateMembership(Long roomId, String userId) {
        if (!roomRepository.existsMemberInRoom(roomId, userId)) {
            throw new UnauthorizedException("User is not a member of this room");
        }
    }

    // --- Bulk Fetching Helpers ---

    private Page<MessageResponse> mapMessagesToResponses(Page<Message> messages, String currentUserId) {
        if (messages.isEmpty()) {
            return Page.empty();
        }

        // 1. Collect Message IDs
        List<Long> messageIds = messages.stream().map(Message::getId).collect(Collectors.toList());

        // 2. Bulk Fetch Data
        Map<Long, List<MessageResponse.AttachmentInfo>> attachmentsMap = fetchAttachments(messageIds);
        Map<Long, Map<String, Integer>> reactionCountsMap = fetchReactionCounts(messageIds);
        Map<Long, List<String>> userReactionsMap = fetchUserReactions(messageIds, currentUserId);

        // 3. Map to DTOs
        return messages.map(message -> mapToResponse(
                message,
                attachmentsMap.getOrDefault(message.getId(), List.of()),
                reactionCountsMap.getOrDefault(message.getId(), Map.of()),
                userReactionsMap.getOrDefault(message.getId(), List.of())));
    }

    private List<MessageResponse> mapMessagesToListResponses(List<Message> messages, String currentUserId) {
        if (messages.isEmpty()) {
            return List.of();
        }

        List<Long> messageIds = messages.stream().map(Message::getId).collect(Collectors.toList());

        Map<Long, List<MessageResponse.AttachmentInfo>> attachmentsMap = fetchAttachments(messageIds);
        Map<Long, Map<String, Integer>> reactionCountsMap = fetchReactionCounts(messageIds);
        Map<Long, List<String>> userReactionsMap = fetchUserReactions(messageIds, currentUserId);

        return messages.stream().map(message -> mapToResponse(
                message,
                attachmentsMap.getOrDefault(message.getId(), List.of()),
                reactionCountsMap.getOrDefault(message.getId(), Map.of()),
                userReactionsMap.getOrDefault(message.getId(), List.of()))).collect(Collectors.toList());
    }

    private Map<Long, List<MessageResponse.AttachmentInfo>> fetchAttachments(List<Long> messageIds) {
        List<MessageAttachment> attachments = attachmentRepository.findByMessageIdIn(messageIds);
        return attachments.stream()
                .collect(Collectors.groupingBy(
                        att -> att.getMessage().getId(),
                        Collectors.mapping(att -> new MessageResponse.AttachmentInfo(
                                att.getId(),
                                att.getFileId(),
                                att.getFileName(),
                                att.getFileType(),
                                att.getFileSize(),
                                att.getAiStatus()), Collectors.toList())));
    }

    private Map<Long, Map<String, Integer>> fetchReactionCounts(List<Long> messageIds) {
        List<Object[]> counts = reactionRepository.countReactionsByMessageIdIn(messageIds);
        Map<Long, Map<String, Integer>> result = new HashMap<>();

        for (Object[] row : counts) {
            Long messageId = (Long) row[0];
            String emoji = (String) row[1];
            Long count = (Long) row[2];

            result.computeIfAbsent(messageId, k -> new HashMap<>())
                    .put(emoji, count.intValue());
        }
        return result;
    }

    private Map<Long, List<String>> fetchUserReactions(List<Long> messageIds, String userId) {
        List<MessageReaction> reactions = reactionRepository.findByMessageIdInAndUserId(messageIds, userId);
        return reactions.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getMessage().getId(),
                        Collectors.mapping(r -> r.getId().getEmoji(), Collectors.toList())));
    }

    // Original method repurposed for single message fetching (backward
    // compatibility / single ops)
    private MessageResponse toMessageResponse(Message message, String currentUserId) {
        // Fetch single message data
        List<MessageResponse.AttachmentInfo> attachments = attachmentRepository.findByMessageId(message.getId())
                .stream()
                .map(att -> new MessageResponse.AttachmentInfo(
                        att.getId(),
                        att.getFileId(),
                        att.getFileName(),
                        att.getFileType(),
                        att.getFileSize(),
                        att.getAiStatus()))
                .collect(Collectors.toList());

        List<Object[]> reactionCounts = reactionRepository.countReactionsByMessageId(message.getId());
        Map<String, Integer> reactionCountsMap = new HashMap<>();
        for (Object[] result : reactionCounts) {
            reactionCountsMap.put((String) result[0], ((Long) result[1]).intValue());
        }

        List<String> userReactions = reactionRepository.findByIdMessageIdAndIdUserId(message.getId(), currentUserId)
                .stream()
                .map(r -> r.getId().getEmoji())
                .collect(Collectors.toList());

        return mapToResponse(message, attachments, reactionCountsMap, userReactions);
    }

    // Core mapping logic
    private MessageResponse mapToResponse(
            Message message,
            List<MessageResponse.AttachmentInfo> attachments,
            Map<String, Integer> reactionCounts,
            List<String> userReactions) {

        // Fetch sender details from User Service (this is still N+1 if not
        // cached/batched, but acceptable for now per requirements)
        // Ideally UserClient should support batch fetching users too.
        MessageResponse.SenderInfo senderInfo;
        try {
            UserClient.UserInfo userInfo = userClient.getUserById(message.getSenderId());
            senderInfo = new MessageResponse.SenderInfo(
                    userInfo.getId(),
                    userInfo.getUsername(),
                    userInfo.getFullName(),
                    userInfo.getAvatarUrl());
        } catch (Exception e) {
            log.warn("Failed to fetch user info for userId: {}, using placeholder", message.getSenderId());
            senderInfo = new MessageResponse.SenderInfo(
                    message.getSenderId(),
                    "user" + message.getSenderId(),
                    "User " + message.getSenderId(),
                    null);
        }

        return MessageResponse.builder()
                .id(message.getId())
                .roomId(message.getChannel().getRoom().getId())
                .sender(senderInfo)
                .content(message.getContent())
                .parentMessageId(message.getParentMessageId())
                .isPinned(message.getIsPinned())
                .isEdited(message.getIsEdited())
                .isDeleted(message.getIsDeleted())
                .attachments(attachments)
                .reactionCounts(reactionCounts)
                .userReactions(userReactions)
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .build();
    }
}
