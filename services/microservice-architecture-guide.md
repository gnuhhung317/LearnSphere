# Kiến trúc Microservice với Maven Parent POM

## 1. Tổng quan về Maven Parent POM

### Maven Parent POM là gì?

Maven Parent POM là một file `pom.xml` cha được sử dụng để:
- **Quản lý tập trung** các dependency versions
- **Chia sẻ configuration** chung cho tất cả modules
- **Đảm bảo consistency** giữa các services
- **Giảm thiểu duplicate code** trong các pom.xml con

### Cấu trúc thư mục

```
studyhub/services/
├── pom.xml                    # Parent POM
├── common/                    # Shared utilities
├── user-service/             
│   ├── pom.xml               # Child POM
│   └── src/
├── chat-service/
│   ├── pom.xml               # Child POM  
│   └── src/
├── auth-service/
│   └── ...
└── ...
```

## 2. Parent POM Analysis

### Thông tin cơ bản
```xml
<groupId>com.duchung.vn</groupId>
<artifactId>studyhub-services</artifactId>
<version>1.0.0-SNAPSHOT</version>
<packaging>pom</packaging>        <!-- Quan trọng: packaging = pom -->
```

### Modules được quản lý
```xml
<modules>
    <module>common</module>
    <module>user-service</module>
    <module>chat-service</module>
    <module>auth-service</module>
    <!-- ... các services khác -->
</modules>
```

### Version Management
```xml
<properties>
    <java.version>21</java.version>
    <spring-cloud.version>2023.0.0</spring-cloud.version>
    <testcontainers.version>1.19.8</testcontainers.version>
    <mapstruct.version>1.5.5.Final</mapstruct.version>
</properties>
```

## 3. Child POM (User Service) Analysis

### Parent Reference
```xml
<parent>
    <groupId>com.duchung.vn</groupId>
    <artifactId>studyhub-services</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>    <!-- Đường dẫn tới parent -->
</parent>
```

### Service Information
```xml
<groupId>com.studyhub</groupId>           <!-- Có thể khác parent -->
<artifactId>user-service</artifactId>
<version>0.0.1-SNAPSHOT</version>
```

## 4. Lợi ích của kiến trúc này

### 🎯 Centralized Management
- Tất cả dependency versions được quản lý ở một nơi
- Dễ dàng upgrade hoặc downgrade versions
- Tránh version conflicts giữa các services

### 🔄 Consistency
- Tất cả services sử dụng cùng Java version (21)
- Cùng Spring Boot version (3.3.0)
- Cùng coding standards và plugins

### 📦 Shared Dependencies
- Common dependencies như PostgreSQL, Lombok được share
- Reduce duplication trong các child POMs
- Easier maintenance

### 🚀 Build Efficiency
- Build tất cả services cùng lúc: `mvn clean install`
- Build specific service: `mvn clean install -pl user-service`
- Parallel builds: `mvn clean install -T 4`

## 5. Cách hoạt động

### Dependency Resolution
1. Child POM kế thừa tất cả dependencies từ Parent POM
2. Child có thể override hoặc add thêm dependencies
3. Versions được resolve từ `<dependencyManagement>` của parent

### Build Process
1. Maven đọc parent POM trước
2. Resolve tất cả managed dependencies
3. Build từng module theo thứ tự dependency
4. Package các artifacts

## 6. Best Practices

### ✅ Do's
- Luôn sử dụng `<dependencyManagement>` trong parent
- Định nghĩa versions trong `<properties>`
- Sử dụng `relativePath` trong child POMs
- Group related services trong cùng parent

### ❌ Don'ts  
- Không hardcode versions trong child POMs
- Không duplicate dependencies giữa parent và child
- Không skip parent reference trong child POMs

## 7. Commands hữu ích

### Build Commands
```bash
# Build tất cả services
mvn clean install

# Build specific service  
mvn clean install -pl user-service

# Build với skip tests
mvn clean install -DskipTests

# Build parallel (4 threads)
mvn clean install -T 4

# Run specific service
cd user-service
mvn spring-boot:run
```

### Dependency Commands
```bash
# Xem dependency tree
mvn dependency:tree

# Xem effective POM
mvn help:effective-pom

# Analyze dependencies
mvn dependency:analyze
```

## 8. Troubleshooting

### Common Issues

#### Issue 1: Parent POM not found
```
Could not find artifact com.duchung.vn:studyhub-services:pom:1.0.0-SNAPSHOT
```
**Solution:** Đảm bảo parent POM được build trước:
```bash
cd studyhub/services
mvn clean install
```

#### Issue 2: Version conflicts
```
Version conflict detected for dependency
```
**Solution:** Sử dụng `<dependencyManagement>` để fix versions

#### Issue 3: Module not found
```
Child module does not exist: user-service
```
**Solution:** Kiểm tra đường dẫn trong `<modules>` section

## 9. Kiến trúc Microservice Pattern

### Service Independence
- Mỗi service có database riêng
- Independent deployment
- Technology diversity (nếu cần)

### Communication
- REST API giữa các services  
- Event-driven với Kafka/RabbitMQ
- Service discovery (nếu có)

### Infrastructure
- Container hóa với Docker
- Kubernetes orchestration
- Monitoring và logging tập trung

## 10. Next Steps

1. **Tạo Common Module**: Shared utilities, DTOs
2. **API Gateway**: Route requests to services
3. **Service Discovery**: Eureka hoặc Consul
4. **Configuration Management**: Spring Cloud Config
5. **Circuit Breaker**: Hystrix hoặc Resilience4j
6. **Distributed Tracing**: Zipkin integration

---

*File này giải thích kiến trúc microservice hiện tại của StudyHub project và cách Maven Parent POM được sử dụng để quản lý multiple services.*