package com.studyhub.media_service.util;

import com.studyhub.media_service.entity.MediaFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

public class FileValidator {

    // Max file sizes in bytes
    public static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final long MAX_DOCUMENT_SIZE = 50 * 1024 * 1024; // 50MB
    public static final long MAX_VIDEO_SIZE = 500 * 1024 * 1024; // 500MB

    // Allowed content types
    private static final List<String> IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp", "image/svg+xml"
    );

    private static final List<String> DOCUMENT_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain"
    );

    private static final List<String> VIDEO_TYPES = Arrays.asList(
            "video/mp4", "video/mpeg", "video/quicktime", "video/x-msvideo", "video/webm"
    );

    private static final List<String> AUDIO_TYPES = Arrays.asList(
            "audio/mpeg", "audio/mp3", "audio/wav", "audio/ogg"
    );

    public static MediaFile.FileType detectFileType(String contentType) {
        if (contentType == null) {
            return MediaFile.FileType.OTHER;
        }

        if (IMAGE_TYPES.contains(contentType.toLowerCase())) {
            return MediaFile.FileType.IMAGE;
        } else if (DOCUMENT_TYPES.contains(contentType.toLowerCase())) {
            return MediaFile.FileType.DOCUMENT;
        } else if (VIDEO_TYPES.contains(contentType.toLowerCase())) {
            return MediaFile.FileType.VIDEO;
        } else if (AUDIO_TYPES.contains(contentType.toLowerCase())) {
            return MediaFile.FileType.AUDIO;
        }

        return MediaFile.FileType.OTHER;
    }

    public static void validateFile(MultipartFile file) throws InvalidFileException {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is empty");
        }

        String contentType = file.getContentType();
        long fileSize = file.getSize();
        MediaFile.FileType fileType = detectFileType(contentType);

        // Validate file size based on type
        switch (fileType) {
            case IMAGE:
                if (fileSize > MAX_IMAGE_SIZE) {
                    throw new InvalidFileException("Image file size exceeds maximum allowed size of 10MB");
                }
                break;
            case DOCUMENT:
                if (fileSize > MAX_DOCUMENT_SIZE) {
                    throw new InvalidFileException("Document file size exceeds maximum allowed size of 50MB");
                }
                break;
            case VIDEO:
                if (fileSize > MAX_VIDEO_SIZE) {
                    throw new InvalidFileException("Video file size exceeds maximum allowed size of 500MB");
                }
                break;
            default:
                if (fileSize > MAX_DOCUMENT_SIZE) {
                    throw new InvalidFileException("File size exceeds maximum allowed size of 50MB");
                }
        }

        // Validate file extension matches content type
        String filename = file.getOriginalFilename();
        if (filename != null) {
            String extension = getFileExtension(filename).toLowerCase();
            if (!isExtensionValid(extension, contentType)) {
                throw new InvalidFileException("File extension does not match content type");
            }
        }
    }

    private static String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }

    private static boolean isExtensionValid(String extension, String contentType) {
        if (contentType == null) {
            return false;
        }

        // Basic validation - can be extended
        switch (extension) {
            case "jpg":
            case "jpeg":
                return contentType.contains("jpeg");
            case "png":
                return contentType.contains("png");
            case "gif":
                return contentType.contains("gif");
            case "webp":
                return contentType.contains("webp");
            case "pdf":
                return contentType.contains("pdf");
            case "doc":
            case "docx":
                return contentType.contains("word") || contentType.contains("document");
            case "mp4":
                return contentType.contains("mp4");
            default:
                return true; // Allow other extensions for now
        }
    }

    public static class InvalidFileException extends Exception {

        public InvalidFileException(String message) {
            super(message);
        }
    }
}
