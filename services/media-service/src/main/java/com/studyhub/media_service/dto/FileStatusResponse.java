package com.studyhub.media_service.dto;

public class FileStatusResponse {

    private String fileId;
    private String status;

    public FileStatusResponse() {
    }

    public FileStatusResponse(String fileId, String status) {
        this.fileId = fileId;
        this.status = status;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
