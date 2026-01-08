package com.studyhub.media_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "media_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaFile {

    @Id
    private String id;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false)
    private String storedFilename;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type")
    private FileType fileType;

    @Column(name = "bucket_name")
    private String bucketName;

    @Column(name = "uploaded_by")
    private String uploadedBy;
    
    @Column(name = "room_id")
    private Long roomId;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private FileStatus status;

    @Column(name = "thumbnail_id")
    private String thumbnailId;

    public enum FileType {
        IMAGE, DOCUMENT, VIDEO, AUDIO, OTHER
    }

    public enum FileStatus {
        PENDING, PROCESSING, READY, FAILED
    }
}
