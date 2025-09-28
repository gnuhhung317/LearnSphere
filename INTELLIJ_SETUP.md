# Hướng Dẫn Sử Dụng IntelliJ IDEA cho StudyHub Microservices

## Cấu Hình Run Configurations

Tôi đã tạo sẵn các **Run Configurations** cho tất cả microservices của bạn. Sau khi mở project trong IntelliJ IDEA, bạn sẽ thấy các configuration này ở góc trên bên phải.

### Các Services Có Sẵn

1. **User Service** - Quản lý người dùng
2. **Auth Service** - Xác thực và phân quyền  
3. **Chat Service** - Dịch vụ chat/tin nhắn
4. **AI Service** - Dịch vụ AI/Machine Learning
5. **Media Service** - Quản lý file media
6. **Search Service** - Tìm kiếm
7. **Realtime Service** - Xử lý realtime (WebSocket)
8. **All StudyHub Services** - Chạy tất cả services cùng lúc

### Cách Sử Dụng

#### 1. Chạy Từng Service Riêng Lẻ

1. Ở góc trên bên phải IntelliJ IDEA, click vào dropdown list
2. Chọn service bạn muốn chạy (ví dụ: "User Service")
3. Click nút **Run** (tam giác xanh) hoặc **Debug** (con bọ)
4. Service sẽ khởi động với profile `dev`

#### 2. Chạy Tất Cả Services Cùng Lúc

1. Chọn "All StudyHub Services" từ dropdown
2. Click **Run** 
3. Tất cả services sẽ khởi động đồng thời trong các tab riêng biệt

#### 3. Quản Lý Services

- **Dừng service**: Click nút **Stop** (hình vuông đỏ) trong console tab
- **Restart service**: Click **Restart** trong console tab
- **Xem logs**: Mỗi service sẽ có tab riêng với logs

### Ports Mặc Định

Các service sẽ chạy trên các port sau:

```
- User Service:     8081
- Auth Service:     8082  
- Chat Service:     8083
- AI Service:       8084
- Media Service:    8085
- Search Service:   8086
- Realtime Service: 8087
```

### Environment Variables

Mỗi service được cấu hình với:
- `SPRING_PROFILES_ACTIVE=dev` - Sử dụng profile development
- Các service sẽ sử dụng H2 in-memory database để test

## Troubleshooting

### Lỗi "Module not found"

Nếu gặp lỗi module không tìm thấy:

1. **Import project như Maven project**:
   - File → Open → Chọn thư mục `studyhub`
   - Chọn "Import as Maven project"

2. **Reload Maven project**:
   - Mở Maven panel (View → Tool Windows → Maven)
   - Click nút **Reload** 

3. **Build project**:
   - Build → Build Project hoặc Ctrl+F9

### Lỗi "Main class not found"

Nếu main class không tìm thấy, có nghĩa là service đó chưa có Application class. Bạn cần tạo:

```java
@SpringBootApplication
public class ServiceNameApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceNameApplication.class, args);
    }
}
```

### Lỗi Port Đã Được Sử Dụng

Nếu port bị conflict:
1. Dừng tất cả services đang chạy
2. Hoặc thay đổi port trong `application-dev.yml` của service đó

### Cấu Hình Database

Services hiện tại sử dụng H2 in-memory database. Để kết nối PostgreSQL:

1. Cập nhật `application-dev.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/studyhub_dev
    username: your_username
    password: your_password
```

2. Đảm bảo PostgreSQL đang chạy

## Tips Sử Dụng

### 1. Hot Reload
- Bật "Build project automatically" trong Settings
- Thay đổi code sẽ được reload tự động

### 2. Debug Multiple Services
- Có thể debug nhiều service cùng lúc
- Mỗi service sẽ có debug session riêng

### 3. Service Dependencies
Thứ tự khởi động được khuyến nghị:
1. Auth Service (trước tiên)
2. User Service  
3. Các service khác

### 4. Monitoring
- Kiểm tra health: `http://localhost:808x/actuator/health`
- Xem metrics: `http://localhost:808x/actuator/metrics`

## Cấu Hình Nâng Cao

### Shared Run Configuration

Các file trong `.idea/runConfigurations/` có thể được:
- Commit vào Git để team cùng sử dụng
- Customize theo nhu cầu riêng

### Environment Profiles

Thay đổi profile bằng cách edit Run Configuration:
- `SPRING_PROFILES_ACTIVE=dev` (development)
- `SPRING_PROFILES_ACTIVE=test` (testing) 
- `SPRING_PROFILES_ACTIVE=prod` (production)

### JVM Options

Thêm JVM options nếu cần:
```
-Xmx512m -Xms256m
-Dspring.profiles.active=dev
-Ddebug=true
```

Với cấu hình này, bạn có thể dễ dàng quản lý và chạy các microservice ngay từ IntelliJ IDEA!