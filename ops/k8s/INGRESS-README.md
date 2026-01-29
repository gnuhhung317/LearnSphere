# StudyHub Kubernetes Ingress Configuration

## Overview
Tệp `ingress.yaml` cung cấp 2 cấu hình Ingress:

1. **Host-based routing** - Mỗi service có subdomain riêng
2. **Path-based routing** - Tất cả services dùng chung domain với đường dẫn khác nhau

## Prerequisites

### 1. Cài đặt NGINX Ingress Controller
```powershell
# Thêm Helm repo
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update

# Cài đặt NGINX Ingress Controller
helm install nginx-ingress ingress-nginx/ingress-nginx `
  --namespace ingress-nginx `
  --create-namespace `
  --set controller.service.type=LoadBalancer
```

### 2. Kiểm tra Ingress Controller đã chạy
```powershell
kubectl get pods -n ingress-nginx
kubectl get svc -n ingress-nginx
```

## Deploy Ingress

### Option 1: Host-based Routing (Recommended)
```powershell
kubectl apply -f ingress.yaml
```

Sau đó cập nhật file `hosts` (Windows: `C:\Windows\System32\drivers\etc\hosts`):
```
127.0.0.1 keycloak.studyhub.local
127.0.0.1 minio-api.studyhub.local
127.0.0.1 minio-console.studyhub.local
127.0.0.1 rabbitmq.studyhub.local
127.0.0.1 postgres-user.studyhub.local
127.0.0.1 postgres-chat.studyhub.local
127.0.0.1 redis.studyhub.local
```

**Access URLs:**
- Keycloak: http://keycloak.studyhub.local
- MinIO Console: http://minio-console.studyhub.local
- MinIO API: http://minio-api.studyhub.local
- RabbitMQ Management: http://rabbitmq.studyhub.local
- PostgreSQL User DB: http://postgres-user.studyhub.local (port 5432)
- PostgreSQL Chat DB: http://postgres-chat.studyhub.local (port 5432)
- Redis: http://redis.studyhub.local (port 6379)

### Option 2: Path-based Routing
Nếu muốn dùng single domain:
```powershell
# Xóa Ingress cũ nếu đã apply
kubectl delete ingress studyhub-ingress -n studyhub-dev

# Apply path-based Ingress
kubectl apply -f - <<EOF
# (Copy nội dung từ studyhub-ingress-path-based trong ingress.yaml)
EOF
```

Cập nhật `hosts`:
```
127.0.0.1 studyhub.local
```

**Access URLs:**
- Keycloak: http://studyhub.local/auth
- MinIO Console: http://studyhub.local/minio-console
- MinIO API: http://studyhub.local/minio-api
- RabbitMQ Management: http://studyhub.local/rabbitmq
- PostgreSQL User DB: http://studyhub.local/postgres-user
- PostgreSQL Chat DB: http://studyhub.local/postgres-chat
- Redis: http://studyhub.local/redis

## Verify Ingress

```powershell
# Kiểm tra Ingress đã được tạo
kubectl get ingress -n studyhub-dev

# Xem chi tiết
kubectl describe ingress studyhub-ingress -n studyhub-dev

# Kiểm tra logs của Ingress Controller
kubectl logs -n ingress-nginx -l app.kubernetes.io/component=controller --tail=100
```

## Troubleshooting

### 1. Ingress không hoạt động
```powershell
# Kiểm tra endpoints
kubectl get endpoints -n studyhub-dev

# Kiểm tra services
kubectl get svc -n studyhub-dev
```

### 2. 404 Not Found
- Kiểm tra service name và port trong Ingress config
- Kiểm tra pod đang chạy: `kubectl get pods -n studyhub-dev`

### 3. Connection refused
- Kiểm tra Ingress Controller: `kubectl get svc -n ingress-nginx`
- Kiểm tra ports đã được expose đúng chưa

## Security Notes

**⚠️ DEV ENVIRONMENT ONLY:**
- Tất cả services đã được expose qua Ingress cho môi trường DEV
- **KHÔNG BAO GIỜ** expose databases qua Ingress trên PRODUCTION
- PostgreSQL và Redis nên chỉ truy cập qua port-forward hoặc internal service trong production

**Kết nối database từ local:**
```powershell
# PostgreSQL User DB
psql -h postgres-user.studyhub.local -p 80 -U studyhub -d studyhub_user

# PostgreSQL Chat DB  
psql -h postgres-chat.studyhub.local -p 80 -U studyhub -d studyhub_chat

# Redis
redis-cli -h redis.studyhub.local -p 80
```

## Production Considerations

Khi deploy lên production, nên:
1. Bật SSL/TLS với cert-manager
2. Cấu hình rate limiting
3. Thêm authentication cho management UIs
4. Sử dụng real domain names
5. Cấu hình CORS policies

### Ví dụ với SSL:
```yaml
metadata:
  annotations:
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  tls:
    - hosts:
        - keycloak.studyhub.com
      secretName: keycloak-tls
```
