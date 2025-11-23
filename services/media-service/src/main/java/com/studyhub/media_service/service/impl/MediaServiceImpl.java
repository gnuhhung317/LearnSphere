package com.studyhub.media_service.service.impl;

import com.studyhub.media_service.entity.MediaFile;
import com.studyhub.media_service.repository.MediaFileRepository;
import com.studyhub.media_service.service.MediaService;
import com.studyhub.media_service.storage.StorageService;
import com.studyhub.media_service.util.FileValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class MediaServiceImpl implements MediaService {

    private final StorageService storageService;
    private final MediaFileRepository mediaFileRepository;

    @Value("${minio.bucket:media}")
    private String defaultBucket;

    @Value("${server.port:8084}")
    private String serverPort;

    @Value("${media.base-url:http://localhost:8084}")
    private String mediaBaseUrl;

    public MediaServiceImpl(StorageService storageService, MediaFileRepository mediaFileRepository) {
        this.storageService = storageService;
        this.mediaFileRepository = mediaFileRepository;
    }

    @Override
    public MediaFile storeFile(MultipartFile file, String uploadedBy) throws Exception {
        // Validate file
        FileValidator.validateFile(file);

        // Generate unique ID and stored filename
        String id = UUID.randomUUID().toString();
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String storedFilename = id + (extension.isEmpty() ? "" : "." + extension);

        // Detect file type
        MediaFile.FileType fileType = FileValidator.detectFileType(file.getContentType());

        // Create MediaFile entity
        MediaFile mediaFile = MediaFile.builder()
                .id(id)
                .originalFilename(originalFilename)
                .storedFilename(storedFilename)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .fileType(fileType)
                .bucketName(defaultBucket)
                .uploadedBy(uploadedBy)
                .uploadedAt(LocalDateTime.now())
                .status(MediaFile.FileStatus.PENDING)
                .build();

        // Save to database first
        mediaFile = mediaFileRepository.save(mediaFile);

        try {
            // Upload to MinIO
            storageService.store(
                    defaultBucket,
                    storedFilename,
                    file.getInputStream(),
                    file.getSize(),
                    file.getContentType()
            );

            // Update status to READY
            mediaFile.setStatus(MediaFile.FileStatus.READY);
            mediaFile = mediaFileRepository.save(mediaFile);

            log.info("File uploaded successfully: {} ({})", originalFilename, id);
        } catch (Exception e) {
            log.error("Failed to upload file: {}", originalFilename, e);
            mediaFile.setStatus(MediaFile.FileStatus.FAILED);
            mediaFileRepository.save(mediaFile);
            throw e;
        }

        return mediaFile;
    }

    @Override
    public String getFileStatus(String fileId) {
        return mediaFileRepository.findById(fileId)
                .map(mf -> mf.getStatus().name())
                .orElse("NOT_FOUND");
    }

    @Override
    public String getFileUrl(String fileId) {
        // Return backend URL instead of direct MinIO URL
        return mediaBaseUrl + "/api/media/files/" + fileId + "/download";
    }

    @Override
    public MediaFile getMediaFile(String fileId) {
        return mediaFileRepository.findById(fileId).orElse(null);
    }

    @Override
    public InputStream getFileContent(String fileId) throws Exception {
        MediaFile mediaFile = getMediaFile(fileId);
        if (mediaFile == null) {
            throw new IllegalArgumentException("File not found: " + fileId);
        }

        return storageService.getObject(mediaFile.getBucketName(), mediaFile.getStoredFilename());
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }
}
