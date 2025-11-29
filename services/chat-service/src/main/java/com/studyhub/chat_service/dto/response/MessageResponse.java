package com.studyhub.chat_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {
    
    private Long id;
    private Long roomId;
    private SenderInfo sender;
    private String content;
    private Long parentMessageId;
    private Boolean isPinned;
    private Boolean isEdited;
    private Boolean isDeleted;
    private List<AttachmentInfo> attachments;
    private Map<String, Integer> reactionCounts; // emoji -> count
    private List<String> userReactions; // Emojis current user reacted with
    private Instant createdAt;
    private Instant updatedAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SenderInfo {
        private Long userId;
        private String username;
        private String fullName;
        private String avatarUrl;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttachmentInfo {
        private String fileId;
        private String fileName;
        private String fileType;
        private Long fileSize;
    }
}
