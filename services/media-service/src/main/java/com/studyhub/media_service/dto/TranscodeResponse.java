package com.studyhub.media_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranscodeResponse {

    private String jobId;
    private String fileId;
    private String status;
    private List<TranscodedVariant> variants;
    private LocalDateTime createdAt;
    private String message;

    public TranscodeResponse(String jobId, String status, String message) {
        this.jobId = jobId;
        this.status = status;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TranscodedVariant {
        private String variantId;
        private String resolution;
        private String codec;
        private Integer bitrate;
        private String status;
        private String url;
    }
}
