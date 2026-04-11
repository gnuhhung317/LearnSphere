package com.studyhub.ai_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
public class MessageDto {
    private String content;
    private SenderInfo sender;
    private Instant createdAt;

    @Data
    @NoArgsConstructor
    public static class SenderInfo {
        private String username;
        private String fullName;
    }
}
