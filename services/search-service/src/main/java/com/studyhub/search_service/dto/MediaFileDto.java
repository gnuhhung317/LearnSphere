package com.studyhub.search_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaFileDto {
    private String id;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private String fileType;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
    private String status;
}
