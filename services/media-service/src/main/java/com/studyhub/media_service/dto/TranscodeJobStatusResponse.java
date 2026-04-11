package com.studyhub.media_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranscodeJobStatusResponse {

    private String jobId;
    private String fileId;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private Integer progress; // 0-100
    private List<TranscodeResponse.TranscodedVariant> variants;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String errorMessage;
}
