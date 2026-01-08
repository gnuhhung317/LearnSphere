package com.studyhub.media_service.controller;

import com.studyhub.media_service.dto.*;
import com.studyhub.media_service.entity.MediaFile;
import com.studyhub.media_service.entity.TranscodeJob;
import com.studyhub.media_service.entity.VideoVariant;
import com.studyhub.media_service.repository.VideoVariantRepository;
import com.studyhub.media_service.service.MediaService;
import com.studyhub.media_service.service.TranscodeService;
import com.studyhub.media_service.util.FileValidator;
import jakarta.validation.Valid;
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/media")
public class MediaController {

    private final MediaService mediaService;
    private final TranscodeService transcodeService;
    private final VideoVariantRepository videoVariantRepository;

    public MediaController(
            MediaService mediaService,
            TranscodeService transcodeService,
            VideoVariantRepository videoVariantRepository) {
        this.mediaService = mediaService;
        this.transcodeService = transcodeService;
        this.videoVariantRepository = videoVariantRepository;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Media Service is running");
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "roomId", required = false) Long roomId,
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
            MediaFile mediaFile;
            if (roomId != null) {
                mediaFile = mediaService.storeFileInRoom(file, uploadedBy, roomId);
            } else {
                mediaFile = mediaService.storeFile(file, uploadedBy);
            }

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
    
    /**
     * Get all files uploaded to a room
     * GET /api/v1/media/rooms/{roomId}/files
     */
    @GetMapping("/rooms/{roomId}/files")
    public ResponseEntity<List<MediaFile>> getRoomFiles(
            @PathVariable Long roomId,
            @RequestParam(value = "type", required = false) String fileType) {
        
        try {
            List<MediaFile> files;
            if (fileType != null) {
                MediaFile.FileType type = MediaFile.FileType.valueOf(fileType.toUpperCase());
                files = mediaService.getRoomFilesByType(roomId, type);
            } else {
                files = mediaService.getRoomFiles(roomId);
            }
            return ResponseEntity.ok(files);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid file type: {}", fileType);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to get room files for room: {}", roomId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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

    // ==================== Transcoding APIs ====================

    /**
     * API 1: Start transcode job for a video
     * POST /api/v1/media/transcode
     */
    @PostMapping("/transcode")
    public ResponseEntity<TranscodeResponse> startTranscode(
            @Valid @RequestBody TranscodeRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            String requestedBy = jwt != null ? jwt.getClaimAsString("preferred_username") : "anonymous";
            TranscodeResponse response = transcodeService.startTranscodeJob(request, requestedBy);

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid transcode request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new TranscodeResponse(null, "ERROR", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to start transcode job", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new TranscodeResponse(null, "ERROR", "Failed to start transcode job"));
        }
    }

    /**
     * API 2: Get transcode job status
     * GET /api/v1/media/transcode/{jobId}
     */
    @GetMapping("/transcode/{jobId}")
    public ResponseEntity<TranscodeJobStatusResponse> getTranscodeStatus(@PathVariable String jobId) {
        try {
            TranscodeJobStatusResponse status = transcodeService.getJobStatus(jobId);
            return ResponseEntity.ok(status);

        } catch (IllegalArgumentException e) {
            log.warn("Job not found: {}", jobId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to get job status: {}", jobId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * API 3: Get all variants for a video file
     * GET /api/v1/media/files/{fileId}/variants
     */
    @GetMapping("/files/{fileId}/variants")
    public ResponseEntity<List<TranscodeResponse.TranscodedVariant>> getVideoVariants(
            @PathVariable String fileId) {

        try {
            List<TranscodeResponse.TranscodedVariant> variants = transcodeService.getVideoVariants(fileId);
            return ResponseEntity.ok(variants);

        } catch (Exception e) {
            log.error("Failed to get video variants for file: {}", fileId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * API 4: Cancel transcode job
     * DELETE /api/v1/media/transcode/{jobId}
     */
    @DeleteMapping("/transcode/{jobId}")
    public ResponseEntity<Void> cancelTranscode(@PathVariable String jobId) {
        try {
            transcodeService.cancelJob(jobId);
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            log.warn("Job not found: {}", jobId);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.warn("Cannot cancel job: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Failed to cancel job: {}", jobId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Download specific video variant
     * GET /api/v1/media/variants/{variantId}/download
     */
    @GetMapping("/variants/{variantId}/download")
    public ResponseEntity<InputStreamResource> downloadVariant(@PathVariable String variantId) {
        try {
            VideoVariant variant = videoVariantRepository.findById(variantId).orElse(null);
            if (variant == null) {
                return ResponseEntity.notFound().build();
            }

            if (variant.getStatus() != VideoVariant.VariantStatus.READY) {
                return ResponseEntity.status(HttpStatus.PROCESSING).build();
            }

            // TODO: Get actual file from storage
            // For MVP, return mock data
            String mockContent = "Mock video variant data for " + variant.getResolution();
            InputStream inputStream = new ByteArrayInputStream(mockContent.getBytes());
            InputStreamResource resource = new InputStreamResource(inputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename=\"" + variant.getStoredFilename() + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (Exception e) {
            log.error("Failed to download variant: {}", variantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
