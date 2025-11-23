package com.studyhub.media_service.storage;

import java.io.InputStream;

public interface StorageService {

    /**
     * Store content and return object key (id).
     */
    String store(String bucket, String objectKey, InputStream content, long size, String contentType) throws Exception;

    /**
     * Return a public/presigned URL for the object if available; otherwise
     * null.
     */
    String getObjectUrl(String bucket, String objectKey) throws Exception;

    /**
     * Retrieve object content as InputStream
     */
    InputStream getObject(String bucket, String objectKey) throws Exception;
}
