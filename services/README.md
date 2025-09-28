# StudyHub Services - Maven Multi-Module Project

This is the parent project for all StudyHub microservices, organized as a Maven multi-module build.

## Project Structure

```
studyhub/services/
├── pom.xml                    # Parent POM with shared dependencies and configuration
├── common/                    # Shared library for DTOs and utilities
│   ├── pom.xml
│   └── src/
├── user-service/             # User management service
│   ├── pom.xml
│   └── src/
├── chat-service/             # Real-time messaging service
│   ├── pom.xml
│   └── src/
├── auth-service/             # Authentication and authorization service
│   ├── pom.xml
│   └── src/
├── media-service/            # File upload and media processing service
│   ├── pom.xml
│   └── src/
├── ai-service/               # AI-powered features service
│   ├── pom.xml
│   └── src/
├── search-service/           # Search and indexing service
│   ├── pom.xml
│   └── src/
└── realtime-service/         # WebRTC signaling and real-time features
    ├── pom.xml
    └── src/
```

## Technology Stack

- **Java 21**
- **Spring Boot 3.3.0**
- **Spring Cloud 2024.0.0-M4**
- **PostgreSQL** (Database)
- **Kafka** (Message broker)
- **Redis** (Caching)
- **Elasticsearch** (Search engine)
- **Testcontainers** (Integration testing)

## Parent POM Features

The parent POM (`studyhub/services/pom.xml`) provides:

### Shared Dependencies
- Spring Boot starters (web, security, data-jpa, etc.)
- Kafka integration
- Elasticsearch support
- OAuth2 resource server
- Testcontainers for testing

### Dependency Management
- Spring Cloud dependencies
- Database drivers (PostgreSQL)
- Jackson for JSON processing
- MapStruct for object mapping
- Lombok for boilerplate reduction

### Plugin Configuration
- Maven compiler plugin (Java 21)
- Spring Boot Maven plugin
- JaCoCo for code coverage
- Maven Surefire/Failsafe for testing

## Common Library

The `common` module contains shared code:
- DTOs (Data Transfer Objects)
- Utility classes
- Common exceptions
- Validation annotations
- Mapping interfaces

## Build Commands

### Build All Modules
```bash
mvn clean compile
```

### Build Specific Module
```bash
mvn clean compile -pl user-service
```

### Run Tests
```bash
mvn test
```

### Package All Services
```bash
mvn clean package
```

### Install to Local Repository
```bash
mvn clean install
```

## Development Guidelines

### Adding a New Service
1. Create new directory under `studyhub/services/`
2. Add module to parent POM `<modules>` section
3. Create service POM inheriting from parent:
   ```xml
   <parent>
       <groupId>com.duchung.vn</groupId>
       <artifactId>studyhub-services</artifactId>
       <version>1.0.0-SNAPSHOT</version>
       <relativePath>../pom.xml</relativePath>
   </parent>
   ```
4. Add common library dependency:
   ```xml
   <dependency>
       <groupId>com.duchung.vn</groupId>
       <artifactId>common</artifactId>
   </dependency>
   ```

### Service Configuration
Each service should have:
- Application properties for different environments
- Spring Boot main class
- Health check endpoints
- Proper logging configuration
- Database migrations (if applicable)

## Infrastructure Dependencies

Services expect these external dependencies:
- PostgreSQL database
- Kafka message broker
- Redis cache
- Elasticsearch cluster
- Keycloak identity provider

## Docker Support

Use the provided Kubernetes manifests in `studyhub/ops/` for containerized deployment.

## Port Forwarding for Development

Use the PowerShell script `port-forward.ps1` to access Kubernetes services locally:
```powershell
.\port-forward.ps1
```

This forwards all services to localhost ports for development access.