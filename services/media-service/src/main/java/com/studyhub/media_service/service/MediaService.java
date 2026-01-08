package com.studyhub.media_service.service;

import com.studyhub.media_service.entity.MediaFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface MediaService {

    /**
     * Store file with metadata and return MediaFile entity
     */
    MediaFile storeFile(MultipartFile file, String uploadedBy) throws Exception;

    /**
     * Get backend URL for file (not direct MinIO URL)
     */
    String getFileUrl(String fileId);

    /**
     * Return processing status for a file
     */
    String getFileStatus(String fileId);

    /**
     * Get MediaFile metadata by ID
     */
    MediaFile getMediaFile(String fileId);

    /**
     * Stream file content from storage
     */
    InputStream getFileContent(String fileId) throws Exception;
    
    /**
     * Store file with room context
     */
    MediaFile storeFileInRoom(MultipartFile file, String uploadedBy, Long roomId) throws Exception;
    
    /**
     * Get all files uploaded to a room
     */
    java.util.List<MediaFile> getRoomFiles(Long roomId);
    
    /**
     * Get files by room and type
     */
    java.util.List<MediaFile> getRoomFilesByType(Long roomId, MediaFile.FileType fileType);
}
