package com.studyhub.media_service.service;

import com.studyhub.media_service.dto.TranscodeJobStatusResponse;
import com.studyhub.media_service.dto.TranscodeRequest;
import com.studyhub.media_service.dto.TranscodeResponse;
import com.studyhub.media_service.entity.TranscodeJob;

import java.util.List;

public interface TranscodeService {

    /**
     * Start transcoding job for a video file
     */
    TranscodeResponse startTranscodeJob(TranscodeRequest request, String requestedBy);

    /**
     * Get status of a transcode job
     */
    TranscodeJobStatusResponse getJobStatus(String jobId);

    /**
     * Get all transcode jobs for a file
     */
    List<TranscodeJob> getFileTranscodeJobs(String fileId);

    /**
     * Cancel a running transcode job
     */
    void cancelJob(String jobId);

    /**
     * Get all available variants for a video file
     */
    List<TranscodeResponse.TranscodedVariant> getVideoVariants(String fileId);
}
