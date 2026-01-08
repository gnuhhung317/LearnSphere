package com.studyhub.chat_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileDTO {
    private Long id;
    private String fileId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String aiStatus;
    private Instant uploadedAt;
    private String senderName;
    private String senderAvatarUrl;
}
