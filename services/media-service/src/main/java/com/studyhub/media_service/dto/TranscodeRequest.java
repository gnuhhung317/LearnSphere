package com.studyhub.media_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranscodeRequest {

    @NotBlank(message = "File ID is required")
    private String fileId;

    @NotNull(message = "Target formats are required")
    private List<TranscodeFormat> targetFormats;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TranscodeFormat {
        private String resolution; // e.g., "1080p", "720p", "480p"
        private String codec; // e.g., "h264", "h265"
        private Integer bitrate; // in kbps
    }
}
