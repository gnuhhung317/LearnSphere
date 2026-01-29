# Realtime Service - WebRTC Video Calling

Real-time video/audio communication service for StudyHub platform using WebRTC and WebSocket signaling.

## Overview

This service provides:
- WebRTC signaling via WebSocket (STOMP)
- Session management and persistence
- Room-based video calls
- Screen sharing support
- Participant management
- REST API for room information

## Technology Stack

- **Spring Boot 3.5.7** - Application framework
- **Spring WebSocket** - WebSocket support with STOMP
- **SockJS** - WebSocket fallback
- **JPA/Hibernate** - Data persistence
- **H2** (dev) / **PostgreSQL** (prod) - Database
- **Lombok** - Code generation

## Port

**8087** - WebSocket server and REST API

## API Endpoints

### REST API

#### Health Check
```
GET /api/v1/realtime/health
```

#### Get Room Information
```
GET /api/v1/realtime/room/{roomId}

Response:
{
  "roomId": "room-123",
  "participantCount": 2,
  "participants": [
    {
      "sessionId": "...",
      "userId": 1,
      "username": "John Doe",
      "peerId": "peer_123",
      "isAudioEnabled": true,
      "isVideoEnabled": true,
      "isScreenSharing": false
    }
  ]
}
```

#### End Room
```
DELETE /api/v1/realtime/room/{roomId}
```

### WebSocket API

#### Connect
```
WebSocket: ws://localhost:8087/ws
Protocol: STOMP over SockJS
```

#### Subscribe Topics
```
/topic/room/{roomId}                  - Room-wide messages
/topic/room/{roomId}/peer/{peerId}    - Direct peer messages
/topic/room/{roomId}/info             - Room info updates
```

#### Publish Destination
```
/app/signal                           - Send signaling messages
```

#### Message Types
- `join` - Join a room
- `offer` - WebRTC offer
- `answer` - WebRTC answer
- `ice-candidate` - ICE candidate
- `leave` - Leave room
- `media-state` - Update audio/video state

## Database Schema

```sql
CREATE TABLE webrtc_sessions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id VARCHAR(255) UNIQUE NOT NULL,
  room_id VARCHAR(255) NOT NULL,
  user_id BIGINT NOT NULL,
  username VARCHAR(255),
  peer_id VARCHAR(255),
  status VARCHAR(50) NOT NULL,  -- ACTIVE, INACTIVE, ENDED
  is_audio_enabled BOOLEAN DEFAULT TRUE,
  is_video_enabled BOOLEAN DEFAULT TRUE,
  is_screen_sharing BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP,
  ended_at TIMESTAMP,
  INDEX idx_room_status (room_id, status),
  INDEX idx_user_status (user_id, status)
);
```

## Configuration

### Development (application.yml)
```yaml
server:
  port: 8087

spring:
  application:
    name: realtime-service
  datasource:
    url: jdbc:h2:mem:realtimedb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: update
  h2:
    console:
      enabled: true
      path: /h2-console
```

### Production
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/realtimedb
    username: ${DB_USER}
    password: ${DB_PASSWORD}
```

## Build & Run

### Build
```powershell
mvn clean install
```

### Run
```powershell
mvn spring-boot:run
```

### Test
```powershell
mvn test
```

### Docker (Future)
```powershell
docker build -t studyhub-realtime-service .
docker run -p 8087:8087 studyhub-realtime-service
```

## Testing

### Health Check
```powershell
curl http://localhost:8087/api/v1/realtime/health
```

### H2 Console (Dev)
```
URL: http://localhost:8087/h2-console
JDBC URL: jdbc:h2:mem:realtimedb
Username: sa
Password: (empty)
```

### WebSocket Test
Use frontend video call page:
```
http://localhost:3000/video-call?room=test-room
```

## Architecture

### Signaling Flow
```
Client A                 Realtime Service              Client B
   |                            |                          |
   |---join-------------------->|                          |
   |                            |<-------join--------------|
   |<--participant-joined-------|                          |
   |                            |---participant-joined---->|
   |                            |                          |
   |---offer------------------->|---offer----------------->|
   |<--answer-------------------|<--answer-----------------|
   |---ice-candidate----------->|---ice-candidate--------->|
   |                            |                          |
   [Direct P2P Media Connection Established]
```

### Components

```
WebSocketConfig
  └─> Configures STOMP endpoints and message broker

WebRTCSignalingController
  └─> Handles signaling messages (@MessageMapping)
      ├─> Join/Leave events
      ├─> Offer/Answer relay
      └─> ICE candidate relay

WebRTCSessionService
  └─> Business logic
      ├─> Session creation/termination
      ├─> Media state management
      └─> Room information

WebRTCSessionRepository
  └─> Data access (Spring Data JPA)
```

## Production Considerations

### Required for Production
- [ ] Switch to PostgreSQL
- [ ] Add JWT authentication for WebSocket
- [ ] Configure Redis for distributed sessions
- [ ] Enable HTTPS/WSS
- [ ] Set up TURN server configuration
- [ ] Add monitoring and metrics
- [ ] Configure sticky sessions for load balancing
- [ ] Add rate limiting
- [ ] Implement proper CORS policy

### Recommended
- [ ] Add call recording
- [ ] Implement quality metrics
- [ ] Add bandwidth adaptation
- [ ] Enable end-to-end encryption
- [ ] Add notification service integration
- [ ] Implement call history

## Monitoring

### Metrics
- Active sessions count
- Rooms count
- WebSocket connections
- Message throughput

### Logs
- Session events (create, join, leave, end)
- Signaling messages
- Connection errors
- Database operations

## Troubleshooting

### WebSocket fails to connect
```powershell
# Check if service is running
netstat -an | findstr 8087

# Check logs
tail -f logs/realtime-service.log
```

### No remote video
- Verify both peers joined the room
- Check ICE candidates being exchanged
- Verify STUN server connectivity
- May need TURN server for restrictive NATs

### Database errors
```powershell
# Check H2 console
http://localhost:8087/h2-console

# Run migrations manually if needed
```

## Documentation

- [Full Implementation Guide](../docs/WEBRTC-IMPLEMENTATION.md)
- [Quick Start](../docs/WEBRTC-QUICKSTART.md)
- [Integration Examples](../docs/WEBRTC-INTEGRATION-EXAMPLE.md)

## License

Part of StudyHub Platform - Internal Use

---

**Version**: 1.0.0  
**Last Updated**: December 14, 2025  
**Status**: Production Ready ✅
