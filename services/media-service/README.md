# Media Service — MinIO integration

This service can store uploaded files in an S3-compatible MinIO instance.

Defaults (configured in `application.properties`):

- endpoint: http://localhost:9000
- accessKey: minioadmin
- secretKey: minioadmin
- bucket: media

How to run MinIO locally (docker):

```powershell
# run MinIO (S3 API on 9000, console on 9001)
docker run -p 9000:9000 -p 9001:9001 -e MINIO_ROOT_USER=minioadmin -e MINIO_ROOT_PASSWORD=minioadmin -v ${PWD}/minio-data:/data quay.io/minio/minio server /data --console-address :9001
```

Or deploy to Kubernetes (we already have a manifest at `studyhub/ops/k8s/minio-deployment.yaml`):

```powershell
# apply to cluster
k apply -f studyhub/ops/k8s/minio-deployment.yaml -n studyhub-dev

# check service and forward ports (if required):
k port-forward svc/minio 9000:9000 9001:9001 -n studyhub-dev
```

How to test upload to media service (assumes media-service runs on localhost:8084):

```bash
# Upload a file
curl -F "file=@./sample.txt" http://localhost:8084/api/media/upload

# Get status
curl http://localhost:8084/api/media/files/<fileId>
```

Notes
- If MinIO is not available at startup tests or the app will not fail — MinIO initialization is resilient in dev/test and will print a warning.
- The service stores objects using a UUID key and exposes a presigned URL (when available) in the upload response.
