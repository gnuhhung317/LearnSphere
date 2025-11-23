package com.studyhub.media_service.repository;

import com.studyhub.media_service.entity.MediaFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaFileRepository extends JpaRepository<MediaFile, String> {

    List<MediaFile> findByUploadedBy(String uploadedBy);

    List<MediaFile> findByFileType(MediaFile.FileType fileType);

    List<MediaFile> findByStatus(MediaFile.FileStatus status);
}
