package com.studyhub.media_service.dto;

public class UploadResponse {

    private String fileId;
    private String message;
    private String url;

    public UploadResponse() {
    }

    public UploadResponse(String fileId, String message) {
        this.fileId = fileId;
        this.message = message;
    }

    public UploadResponse(String fileId, String message, String url) {
        this.fileId = fileId;
        this.message = message;
        this.url = url;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
