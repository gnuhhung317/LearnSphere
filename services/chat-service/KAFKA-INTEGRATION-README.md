# Kafka Integration - StudyHub Chat Service

## 📋 Overview

Đã tích hợp **Apache Kafka** vào Chat Service để implement event-driven architecture cho StudyHub microservices platform.

### ✅ Lợi ích của Kafka vs RabbitMQ

| Feature | Kafka ✅ | RabbitMQ |
|---------|---------|----------|
| **Throughput** | ~1M msg/s | ~20K msg/s |
| **Event Persistence** | Luôn lưu trữ | Optional |
| **Event Replay** | ✅ Có | ❌ Không |
| **Scalability** | Excellent | Good |
| **Use Case** | Event streaming | Task queues |

**Quyết định:** Chọn Kafka vì phù hợp với event-driven microservices và cần event replay capability.

---

## 🚀 Implementation Summary

### 1. Dependencies Added

**chat-service/pom.xml:**
```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

### 2. Kafka Topics Created

| Topic Name | Purpose | Partitions |
|------------|---------|------------|
| `chat.messages.created` | New messages | 3 |
| `chat.messages.edited` | Message edits | 3 |
| `chat.messages.deleted` | Message deletions | 3 |
| `chat.rooms.created` | New rooms | 3 |
| `chat.rooms.user-joined` | User joins room | 3 |
| `chat.rooms.user-left` | User leaves room | 3 |

### 3. Event Models

**ChatMessageCreatedEvent:**
```java
{
  "messageId": 123,
  "roomId": 1,
  "channelId": 5,
  "senderId": 42,
  "senderUsername": "john_doe",
  "content": "Hello World!",
  "messageType": "TEXT",
  "createdAt": "2025-12-14T10:30:00",
  "eventId": "uuid",
  "timestamp": 1734172200000
}
```

**RoomCreatedEvent:**
```java
{
  "roomId": 1,
  "roomName": "Study Group",
  "description": "CS101 Study Group",
  "creatorId": 42,
  "roomType": "GROUP",
  "isPublic": true,
  "createdAt": "2025-12-14T10:30:00",
  "eventId": "uuid",
  "timestamp": 1734172200000
}
```

### 4. Services Modified

#### MessageService
- ✅ Publishes `ChatMessageCreated` after sending message
- ✅ Publishes `MessageEdited` after editing
- ✅ Publishes `MessageDeleted` after deletion

#### RoomService
- ✅ Publishes `RoomCreated` after room creation
- ✅ Publishes `UserJoinedRoom` after user joins

### 5. Sample Consumer

[SampleEventConsumer.java](src/main/java/com/studyhub/chat_service/consumer/SampleEventConsumer.java) demonstrates how other services would consume events:

- **Search Service:** Index messages for full-text search
- **Notification Service:** Send push notifications
- **Analytics Service:** Track metrics
- **Audit Service:** Log for compliance

---

## 🐳 Docker Compose Setup

**Updated ops/docker-compose.yml:**

```yaml
zookeeper:
  image: confluentinc/cp-zookeeper:7.5.3
  environment:
    ZOOKEEPER_CLIENT_PORT: 2181

kafka:
  image: confluentinc/cp-kafka:7.5.3
  ports:
    - "9092:9092"  # Host access
  environment:
    KAFKA_ZOOKEEPER_CONNECT: 'zookeeper:2181'
    KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
    KAFKA_AUTO_CREATE_TOPICS_ENABLE: 'true'
```

---

## 🧪 Testing Locally

### 1. Start Infrastructure

```powershell
cd studyhub/ops
docker-compose up -d zookeeper kafka postgres-chat-db keycloak
```

Wait ~30 seconds for Kafka to be ready.

### 2. Verify Kafka is Running

```powershell
# Check containers
docker ps | grep kafka

# Check Kafka logs
docker logs kafka
```

### 3. Run Chat Service

```powershell
cd studyhub/services/chat-service
mvn spring-boot:run
```

Watch logs for:
```
✅ Created Kafka topics: chat.messages.created, chat.rooms.created, etc.
✅ Kafka consumer started: SampleEventConsumer
```

### 4. Send Test Message (via REST API)

```powershell
# 1. Create a room
POST http://localhost:8083/api/v1/rooms
{
  "name": "Test Room",
  "description": "Testing Kafka",
  "isPublic": true
}

# 2. Send a message
POST http://localhost:8083/api/v1/rooms/{roomId}/channels/{channelId}/messages
{
  "content": "Hello Kafka!"
}
```

### 5. Check Logs for Kafka Events

**Producer (Chat Service):**
```
✅ Published ChatMessageCreated event: messageId=1, roomId=1
```

**Consumer (Sample Consumer):**
```
📨 Consumed ChatMessageCreated event: messageId=1, roomId=1, content='Hello Kafka!'
```

---

## 📊 Monitoring Kafka

### View Topics

```powershell
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:29092
```

Expected output:
```
chat.messages.created
chat.messages.edited
chat.messages.deleted
chat.rooms.created
chat.rooms.user-joined
```

### View Messages in Topic

```powershell
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:29092 \
  --topic chat.messages.created \
  --from-beginning
```

---

## 🔧 Configuration

**application.yml:**
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: chat-service-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"
```

---

## 🎯 Next Steps

### Sprint 3-4 Completion
- ✅ Chat message persistence
- ✅ REST APIs (send, edit, delete, reactions, pin)
- ✅ WebSocket real-time updates
- ✅ Kafka event publishing

### Sprint 5-6: WebRTC Signaling
- Implement realtime-service for voice/video calls
- WebRTC signaling server

### Sprint 9-10: AI & Search
- **Search Service:** Consume `chat.messages.created` → Index to Elasticsearch
- **AI Service:** Consume messages → Generate summaries, Q&A

---

## 🐛 Troubleshooting

### Issue: Kafka connection refused

**Solution:**
```powershell
# Restart Kafka
docker-compose restart kafka

# Check Kafka is listening on 9092
docker exec -it kafka netstat -tuln | grep 9092
```

### Issue: Messages not consumed

**Solution:**
```powershell
# Check consumer group
docker exec -it kafka kafka-consumer-groups \
  --bootstrap-server localhost:29092 \
  --describe --group chat-service-group

# Reset offsets to replay
docker exec -it kafka kafka-consumer-groups \
  --bootstrap-server localhost:29092 \
  --group chat-service-group \
  --reset-offsets --to-earliest \
  --topic chat.messages.created \
  --execute
```

---

## 📚 References

- [Spring Kafka Docs](https://docs.spring.io/spring-kafka/reference/)
- [Apache Kafka Quickstart](https://kafka.apache.org/quickstart)
- [Event-Driven Architecture Guide](../../studyhub-docs/architecture/)

---

**✅ Implementation Complete!**  
Kafka đã được tích hợp thành công vào Chat Service, cung cấp nền tảng vững chắc cho event-driven microservices architecture.
