package com.studyhub.media_service.controller;

import com.studyhub.media_service.dto.FileStatusResponse;
import com.studyhub.media_service.dto.UploadResponse;
import com.studyhub.media_service.entity.MediaFile;
import com.studyhub.media_service.service.MediaService;
import com.studyhub.media_service.util.FileValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Slf4j
@RestController
@RequestMapping("/api/v1/media")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Media Service is running");
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(new UploadResponse(null, "No file uploaded"));
        }

        try {
            // Validate file
            FileValidator.validateFile(file);

            // Get user from JWT
            String uploadedBy = jwt != null ? jwt.getClaimAsString("preferred_username") : "anonymous";

            // Store file with metadata
            MediaFile mediaFile = mediaService.storeFile(file, uploadedBy);

            // Get backend URL
            String url = mediaService.getFileUrl(mediaFile.getId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new UploadResponse(mediaFile.getId(), "File uploaded successfully", url));

        } catch (FileValidator.InvalidFileException e) {
            log.warn("Invalid file upload: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new UploadResponse(null, "Invalid file: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to upload file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new UploadResponse(null, "Upload failed: " + e.getMessage()));
        }
    }

    @GetMapping("/files/{fileId}")
    public ResponseEntity<FileStatusResponse> getFileStatus(@PathVariable String fileId) {
        String status = mediaService.getFileStatus(fileId);
        if ("NOT_FOUND".equals(status)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new FileStatusResponse(fileId, status));
    }

    @GetMapping("/files/{fileId}/download")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String fileId) {
        try {
            MediaFile mediaFile = mediaService.getMediaFile(fileId);
            if (mediaFile == null) {
                return ResponseEntity.notFound().build();
            }

            if (mediaFile.getStatus() != MediaFile.FileStatus.READY) {
                return ResponseEntity.status(HttpStatus.PROCESSING)
                        .build();
            }

            // Get file content from storage
            InputStream inputStream = mediaService.getFileContent(fileId);
            InputStreamResource resource = new InputStreamResource(inputStream);

            // Set proper headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(mediaFile.getContentType()));
            headers.setContentLength(mediaFile.getFileSize());

            // Use inline for images, attachment for downloads
            if (mediaFile.getFileType() == MediaFile.FileType.IMAGE) {
                headers.add(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + mediaFile.getOriginalFilename() + "\"");
            } else {
                headers.add(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + mediaFile.getOriginalFilename() + "\"");
            }

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (Exception e) {
            log.error("Failed to download file: {}", fileId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
