package com.studyhub.media_service.repository;

import com.studyhub.media_service.entity.TranscodeJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TranscodeJobRepository extends JpaRepository<TranscodeJob, String> {

    List<TranscodeJob> findByFileIdOrderByCreatedAtDesc(String fileId);

    List<TranscodeJob> findByStatusIn(List<TranscodeJob.JobStatus> statuses);
}
