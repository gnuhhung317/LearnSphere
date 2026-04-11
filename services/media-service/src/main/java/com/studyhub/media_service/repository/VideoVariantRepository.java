package com.studyhub.media_service.repository;

import com.studyhub.media_service.entity.VideoVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoVariantRepository extends JpaRepository<VideoVariant, String> {

    List<VideoVariant> findByJobId(String jobId);

    List<VideoVariant> findByFileId(String fileId);
}
