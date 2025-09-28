# User Service - Hướng dẫn chạy

## 🚀 Cách chạy User Service

### 1. Build Project (từ thư mục root services)
```bash
# Mở PowerShell và cd đến thư mục services
cd d:\Code\Projects\learning-project\studyhub\services

# Build toàn bộ project (bao gồm parent POM)
mvn clean install -DskipTests

# Hoặc build chỉ user-service
mvn clean install -pl user-service -DskipTests
```

### 2. Chạy User Service
```bash
# Cách 1: Từ thư mục user-service
cd user-service
mvn spring-boot:run

# Cách 2: Chạy JAR file (sau khi build)
cd user-service/target
java -jar user-service-0.0.1-SNAPSHOT.jar

# Cách 3: Từ thư mục services với profile
mvn spring-boot:run -pl user-service -Dspring-boot.run.profiles=dev
```

### 3. Kiểm tra Service hoạt động

Service sẽ chạy trên port **8081**

#### Health Check
```bash
curl http://localhost:8081/api/users/health
# Response: "User Service is running!"
```

#### Test Endpoint
```bash
curl http://localhost:8081/api/users/test
# Response: "Hello from User Service on port 8081!"
```

#### H2 Database Console (Development)
- URL: http://localhost:8081/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (để trống)

## 📋 API Endpoints

### 1. Tạo User mới
```bash
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "bio": "Software Developer"
  }'
```

### 2. Lấy tất cả Users
```bash
curl http://localhost:8081/api/users
```

### 3. Lấy User theo ID
```bash
curl http://localhost:8081/api/users/1
```

### 4. Lấy User theo Username
```bash
curl http://localhost:8081/api/users/username/john_doe
```

### 5. Cập nhật Last Login
```bash
curl -X PATCH http://localhost:8081/api/users/john_doe/last-login
```

## 🛠️ Troubleshooting

### Issue 1: Maven Build Failed
```
Could not find artifact com.duchung.vn:studyhub-services:pom:1.0.0-SNAPSHOT
```
**Solution:**
```bash
# Build parent POM trước
cd d:\Code\Projects\learning-project\studyhub\services
mvn clean install -N  # -N = Non-recursive (chỉ parent)
```

### Issue 2: Port đã được sử dụng
```
Port 8081 was already in use
```
**Solutions:**
```bash
# Cách 1: Kill process sử dụng port 8081
netstat -ano | findstr :8081
taskkill /PID <PID_NUMBER> /F

# Cách 2: Thay đổi port trong application.yaml
server:
  port: 8082
```

### Issue 3: Database Connection Error
Service sử dụng H2 in-memory database cho development, không cần cài đặt PostgreSQL.

### Issue 4: Compilation Error
```bash
# Clean và compile lại
mvn clean compile
mvn clean install -DskipTests
```

## 🔧 Configuration Files

### application.yaml (Main)
- Server port: 8081
- Active profile: dev
- JPA settings với PostgreSQL

### application-dev.yaml
- H2 in-memory database
- H2 console enabled
- Debug logging
- No OAuth2 security

## 📊 Monitoring & Health Checks

### Actuator Endpoints
```bash
# Health check
curl http://localhost:8081/actuator/health

# Application info  
curl http://localhost:8081/actuator/info

# Metrics
curl http://localhost:8081/actuator/metrics

# Prometheus metrics
curl http://localhost:8081/actuator/prometheus
```

## 🎯 Next Steps

1. **Tích hợp với PostgreSQL**: Thay đổi profile thành `prod`
2. **Thêm Security**: Cấu hình Keycloak OAuth2
3. **API Documentation**: Thêm Swagger/OpenAPI
4. **Testing**: Viết Unit tests và Integration tests
5. **Docker**: Containerize service

## 💡 Tips

- Sử dụng profile `dev` cho development (H2 database)
- Sử dụng profile `prod` cho production (PostgreSQL)
- Kiểm tra logs tại console để debug issues
- H2 console rất hữu ích để xem data trong development