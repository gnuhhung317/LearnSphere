package com.studyhub.media_service.storage;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;

@Service
public class MinioStorageService implements StorageService {

    private final MinioClient client;

    @Value("${minio.bucket:media}")
    private String bucket;

    public MinioStorageService(@Value("${minio.endpoint:http://localhost:9000}") String endpoint,
            @Value("${minio.accessKey:minioadmin}") String accessKey,
            @Value("${minio.secretKey:minioadmin}") String secretKey) {
        this.client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @PostConstruct
    public void ensureBucket() {
        try {
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                client.makeBucket(io.minio.MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception ex) {
            // Do not fail application startup if MinIO is not available during tests/dev runs.
            // Log at debug/warn level and continue. Tests will mock storage service or run without MinIO.
            System.err.println("Warning: cannot ensure MinIO bucket (service may be offline): " + ex.getMessage());
        }
    }

    @Override
    public String store(String bucket, String objectKey, InputStream content, long size, String contentType) throws Exception {
        try {
            PutObjectArgs putArgs = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(content, size, -1)
                    .contentType(contentType)
                    .build();
            client.putObject(putArgs);
            return objectKey;
        } catch (MinioException ex) {
            throw ex;
        }
    }

    @Override
    public String getObjectUrl(String bucket, String objectKey) throws Exception {
        try {
            // Return a simple presigned URL if possible
            return client.getPresignedObjectUrl(io.minio.GetPresignedObjectUrlArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .method(io.minio.http.Method.GET)
                    .build());
        } catch (MinioException ex) {
            return null;
        }
    }

    @Override
    public InputStream getObject(String bucket, String objectKey) throws Exception {
        try {
            return client.getObject(io.minio.GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
        } catch (MinioException ex) {
            throw ex;
        }
    }
}
