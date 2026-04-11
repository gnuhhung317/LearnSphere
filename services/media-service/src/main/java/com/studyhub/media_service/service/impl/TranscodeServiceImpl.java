package com.studyhub.media_service.service.impl;

import com.studyhub.media_service.dto.TranscodeJobStatusResponse;
import com.studyhub.media_service.dto.TranscodeRequest;
import com.studyhub.media_service.dto.TranscodeResponse;
import com.studyhub.media_service.entity.MediaFile;
import com.studyhub.media_service.entity.TranscodeJob;
import com.studyhub.media_service.entity.VideoVariant;
import com.studyhub.media_service.repository.MediaFileRepository;
import com.studyhub.media_service.repository.TranscodeJobRepository;
import com.studyhub.media_service.repository.VideoVariantRepository;
import com.studyhub.media_service.service.TranscodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TranscodeServiceImpl implements TranscodeService {

    private final TranscodeJobRepository transcodeJobRepository;
    private final VideoVariantRepository videoVariantRepository;
    private final MediaFileRepository mediaFileRepository;

    @Value("${media.base-url:http://localhost:8084}")
    private String mediaBaseUrl;

    public TranscodeServiceImpl(
            TranscodeJobRepository transcodeJobRepository,
            VideoVariantRepository videoVariantRepository,
            MediaFileRepository mediaFileRepository) {
        this.transcodeJobRepository = transcodeJobRepository;
        this.videoVariantRepository = videoVariantRepository;
        this.mediaFileRepository = mediaFileRepository;
    }

    @Override
    @Transactional
    public TranscodeResponse startTranscodeJob(TranscodeRequest request, String requestedBy) {
        // Validate file exists and is a video
        MediaFile mediaFile = mediaFileRepository.findById(request.getFileId())
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + request.getFileId()));

        if (mediaFile.getFileType() != MediaFile.FileType.VIDEO) {
            throw new IllegalArgumentException("File is not a video: " + request.getFileId());
        }

        // Create transcode job
        String jobId = UUID.randomUUID().toString();
        TranscodeJob job = TranscodeJob.builder()
                .id(jobId)
                .fileId(request.getFileId())
                .status(TranscodeJob.JobStatus.PENDING)
                .progress(0)
                .requestedBy(requestedBy)
                .createdAt(LocalDateTime.now())
                .build();

        job = transcodeJobRepository.save(job);

        // Create video variants
        List<VideoVariant> variants = request.getTargetFormats().stream()
                .map(format -> VideoVariant.builder()
                        .id(UUID.randomUUID().toString())
                        .jobId(jobId)
                        .fileId(request.getFileId())
                        .resolution(format.getResolution())
                        .codec(format.getCodec())
                        .bitrate(format.getBitrate())
                        .status(VideoVariant.VariantStatus.PENDING)
                        .bucketName(mediaFile.getBucketName())
                        .build())
                .collect(Collectors.toList());

        videoVariantRepository.saveAll(variants);

        log.info("Created transcode job {} for file {} with {} variants",
                jobId, request.getFileId(), variants.size());

        // TODO: Send message to RabbitMQ/Kafka for async processing
        // For now, we'll just mark it as processing
        processTranscodeJobAsync(jobId);

        return new TranscodeResponse(
                jobId,
                TranscodeJob.JobStatus.PENDING.name(),
                "Transcode job created successfully"
        );
    }

    @Override
    public TranscodeJobStatusResponse getJobStatus(String jobId) {
        TranscodeJob job = transcodeJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

        List<VideoVariant> variants = videoVariantRepository.findByJobId(jobId);

        List<TranscodeResponse.TranscodedVariant> variantResponses = variants.stream()
                .map(v -> new TranscodeResponse.TranscodedVariant(
                        v.getId(),
                        v.getResolution(),
                        v.getCodec(),
                        v.getBitrate(),
                        v.getStatus().name(),
                        v.getStatus() == VideoVariant.VariantStatus.READY
                                ? mediaBaseUrl + "/api/v1/media/variants/" + v.getId() + "/download"
                                : null
                ))
                .collect(Collectors.toList());

        return new TranscodeJobStatusResponse(
                job.getId(),
                job.getFileId(),
                job.getStatus().name(),
                job.getProgress(),
                variantResponses,
                job.getCreatedAt(),
                job.getCompletedAt(),
                job.getErrorMessage()
        );
    }

    @Override
    public List<TranscodeJob> getFileTranscodeJobs(String fileId) {
        return transcodeJobRepository.findByFileIdOrderByCreatedAtDesc(fileId);
    }

    @Override
    @Transactional
    public void cancelJob(String jobId) {
        TranscodeJob job = transcodeJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

        if (job.getStatus() == TranscodeJob.JobStatus.COMPLETED ||
                job.getStatus() == TranscodeJob.JobStatus.FAILED) {
            throw new IllegalStateException("Cannot cancel job in status: " + job.getStatus());
        }

        job.setStatus(TranscodeJob.JobStatus.CANCELLED);
        job.setCompletedAt(LocalDateTime.now());
        transcodeJobRepository.save(job);

        log.info("Cancelled transcode job: {}", jobId);
    }

    @Override
    public List<TranscodeResponse.TranscodedVariant> getVideoVariants(String fileId) {
        List<VideoVariant> variants = videoVariantRepository.findByFileId(fileId);

        return variants.stream()
                .filter(v -> v.getStatus() == VideoVariant.VariantStatus.READY)
                .map(v -> new TranscodeResponse.TranscodedVariant(
                        v.getId(),
                        v.getResolution(),
                        v.getCodec(),
                        v.getBitrate(),
                        v.getStatus().name(),
                        mediaBaseUrl + "/api/v1/media/variants/" + v.getId() + "/download"
                ))
                .collect(Collectors.toList());
    }

    /**
     * Simulate async processing (in real implementation, this would be handled by RabbitMQ/Kafka consumer)
     */
    private void processTranscodeJobAsync(String jobId) {
        // TODO: Send to message queue for processing
        // For MVP, we'll simulate immediate completion for demo purposes
        new Thread(() -> {
            try {
                Thread.sleep(2000); // Simulate processing time

                TranscodeJob job = transcodeJobRepository.findById(jobId).orElse(null);
                if (job != null && job.getStatus() == TranscodeJob.JobStatus.PENDING) {
                    job.setStatus(TranscodeJob.JobStatus.PROCESSING);
                    job.setProgress(50);
                    transcodeJobRepository.save(job);

                    Thread.sleep(3000);

                    // Mark variants as ready
                    List<VideoVariant> variants = videoVariantRepository.findByJobId(jobId);
                    variants.forEach(v -> {
                        v.setStatus(VideoVariant.VariantStatus.READY);
                        v.setStoredFilename(job.getFileId() + "_" + v.getResolution() + ".mp4");
                        v.setFileSize(1024L * 1024 * 10); // Mock file size
                    });
                    videoVariantRepository.saveAll(variants);

                    job.setStatus(TranscodeJob.JobStatus.COMPLETED);
                    job.setProgress(100);
                    job.setCompletedAt(LocalDateTime.now());
                    transcodeJobRepository.save(job);

                    log.info("Transcode job {} completed", jobId);
                }
            } catch (Exception e) {
                log.error("Failed to process transcode job {}", jobId, e);
                TranscodeJob job = transcodeJobRepository.findById(jobId).orElse(null);
                if (job != null) {
                    job.setStatus(TranscodeJob.JobStatus.FAILED);
                    job.setErrorMessage(e.getMessage());
                    job.setCompletedAt(LocalDateTime.now());
                    transcodeJobRepository.save(job);
                }
            }
        }).start();
    }
}
