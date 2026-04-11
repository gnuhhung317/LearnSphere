package com.studyhub.media_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "video_variants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoVariant {

    @Id
    private String id;

    @Column(name = "job_id", nullable = false)
    private String jobId;

    @Column(name = "file_id", nullable = false)
    private String fileId;

    @Column(name = "resolution")
    private String resolution; // e.g., "1080p", "720p"

    @Column(name = "codec")
    private String codec;

    @Column(name = "bitrate")
    private Integer bitrate; // in kbps

    @Column(name = "stored_filename")
    private String storedFilename;

    @Column(name = "bucket_name")
    private String bucketName;

    @Column(name = "file_size")
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private VariantStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum VariantStatus {
        PENDING, PROCESSING, READY, FAILED
    }
}
