# WebRTC Video Call Feature - Implementation Guide

## Overview

This document describes the complete WebRTC video call implementation for StudyHub, including real-time peer-to-peer video/audio communication, screen sharing, and WebSocket signaling.

## Architecture

### Backend (Realtime Service)
- **Port**: 8087
- **Framework**: Spring Boot 3.5.7 with WebSocket support
- **Protocol**: STOMP over SockJS
- **Database**: H2 (dev), PostgreSQL (prod)

### Frontend
- **Framework**: Next.js 14 with React 18
- **WebRTC**: Native WebRTC APIs
- **Signaling**: STOMP client with SockJS fallback

### Components

```
Backend:
├── WebSocketConfig         # WebSocket/STOMP configuration
├── WebRTCSession (Entity)  # Session persistence
├── WebRTCSessionService    # Business logic
├── WebRTCSignalingController # WebSocket message handler
└── RealtimeController      # REST API endpoints

Frontend:
├── webrtc.service.ts       # WebRTC core logic
├── useWebRTC.ts            # React hook
├── VideoRoom.tsx           # Main room component
├── VideoTile.tsx           # Individual video display
├── VideoControls.tsx       # Control buttons
└── /video-call/page.tsx    # Next.js page
```

## WebSocket Topics

### Subscribe (Client receives)
- `/topic/room/{roomId}` - Room-wide messages
- `/topic/room/{roomId}/peer/{peerId}` - Direct peer messages
- `/topic/room/{roomId}/info` - Room info updates

### Publish (Client sends)
- `/app/signal` - All signaling messages

## Signaling Message Types

### 1. Join Room
```json
{
  "type": "join",
  "roomId": "room-123",
  "userId": 1,
  "username": "John Doe",
  "peerId": "peer_abc123"
}
```

### 2. WebRTC Offer
```json
{
  "type": "offer",
  "roomId": "room-123",
  "userId": 1,
  "peerId": "peer_abc123",
  "targetPeerId": "peer_xyz789",
  "payload": {
    "type": "offer",
    "sdp": "..."
  }
}
```

### 3. WebRTC Answer
```json
{
  "type": "answer",
  "roomId": "room-123",
  "userId": 1,
  "peerId": "peer_abc123",
  "targetPeerId": "peer_xyz789",
  "payload": {
    "type": "answer",
    "sdp": "..."
  }
}
```

### 4. ICE Candidate
```json
{
  "type": "ice-candidate",
  "roomId": "room-123",
  "userId": 1,
  "peerId": "peer_abc123",
  "targetPeerId": "peer_xyz789",
  "payload": {
    "candidate": "...",
    "sdpMid": "...",
    "sdpMLineIndex": 0
  }
}
```

### 5. Leave Room
```json
{
  "type": "leave",
  "roomId": "room-123",
  "userId": 1,
  "peerId": "peer_abc123",
  "sessionId": "session-uuid"
}
```

### 6. Media State Change
```json
{
  "type": "media-state",
  "roomId": "room-123",
  "userId": 1,
  "peerId": "peer_abc123",
  "sessionId": "session-uuid",
  "payload": {
    "audio": true,
    "video": false,
    "screenSharing": false
  }
}
```

## REST API Endpoints

### Get Room Information
```
GET /api/v1/realtime/room/{roomId}

Response:
{
  "roomId": "room-123",
  "participantCount": 3,
  "participants": [
    {
      "sessionId": "session-uuid",
      "userId": 1,
      "username": "John Doe",
      "peerId": "peer_abc123",
      "isAudioEnabled": true,
      "isVideoEnabled": true,
      "isScreenSharing": false
    }
  ]
}
```

### End Room
```
DELETE /api/v1/realtime/room/{roomId}

Response:
{
  "message": "Room ended successfully",
  "roomId": "room-123"
}
```

### Health Check
```
GET /api/v1/realtime/health

Response:
{
  "status": "UP",
  "service": "realtime-service",
  "timestamp": "2025-12-14T10:30:00"
}
```

## Database Schema

### webrtc_sessions Table
```sql
CREATE TABLE webrtc_sessions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id VARCHAR(255) UNIQUE NOT NULL,
  room_id VARCHAR(255) NOT NULL,
  user_id BIGINT NOT NULL,
  username VARCHAR(255),
  peer_id VARCHAR(255),
  status VARCHAR(50) NOT NULL,
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

## Setup Instructions

### 1. Backend Setup

#### Build the service
```powershell
cd studyhub/services/realtime-service
mvn clean install
```

#### Run the service
```powershell
mvn spring-boot:run
```

The service will start on port 8087.

### 2. API Gateway Configuration

The API Gateway routes WebSocket connections:

**HTTP Routes:**
```yaml
- id: realtime-service-api
  uri: http://localhost:8087
  predicates:
    - Path=/api/v1/realtime/**
```

**WebSocket Routes:**
```yaml
- id: realtime-service-ws
  uri: ws://localhost:8087
  predicates:
    - Path=/ws/**
```

### 3. Frontend Setup

#### Install dependencies (already installed)
```powershell
cd studyhub/frontend
pnpm install
```

#### Usage in a component
```tsx
import { VideoRoom } from '@/components/video/VideoRoom';

function MyPage() {
  return (
    <VideoRoom
      roomId="my-room-123"
      userId={user.id}
      username={user.fullName}
      onLeave={() => router.push('/dashboard')}
    />
  );
}
```

#### Or use the hook directly
```tsx
import { useWebRTC } from '@/hooks/useWebRTC';

function CustomVideoComponent() {
  const {
    isConnected,
    localStream,
    participants,
    connect,
    disconnect,
    toggleAudio,
    toggleVideo,
  } = useWebRTC('room-123', userId, username);

  // Your custom UI logic
}
```

## Usage Examples

### 1. Join a Video Call
```
Navigate to: http://localhost:3000/video-call?room=my-room-123
```

### 2. Programmatically Create a Room
```tsx
import { useRouter } from 'next/navigation';

function CreateRoomButton() {
  const router = useRouter();
  
  const handleCreateRoom = () => {
    const roomId = `room-${Date.now()}`;
    router.push(`/video-call?room=${roomId}`);
  };
  
  return <button onClick={handleCreateRoom}>Create Video Room</button>;
}
```

## Features

### ✅ Implemented
- [x] WebSocket signaling with STOMP
- [x] Peer-to-peer video/audio streaming
- [x] Multiple participants support
- [x] Audio mute/unmute
- [x] Video on/off
- [x] Screen sharing
- [x] Session management
- [x] Participant list
- [x] Room info API
- [x] Responsive video grid
- [x] Connection state management
- [x] Error handling

### 🚧 Future Enhancements
- [ ] Recording functionality
- [ ] Chat integration during calls
- [ ] Raise hand feature
- [ ] Breakout rooms
- [ ] Virtual backgrounds
- [ ] Noise suppression
- [ ] Bandwidth optimization
- [ ] TURN server configuration
- [ ] E2E encryption
- [ ] Call quality metrics

## Testing

### 1. Start the Backend
```powershell
# Start infrastructure
cd studyhub/ops
docker-compose up -d

# Start realtime service
cd ../services/realtime-service
mvn spring-boot:run

# Start API Gateway
cd ../api-gateway
mvn spring-boot:run
```

### 2. Start the Frontend
```powershell
cd studyhub/frontend
pnpm dev
```

### 3. Test Video Call
1. Open `http://localhost:3000/video-call?room=test-room` in Chrome
2. Open the same URL in another Chrome window (incognito)
3. Click "Join Call" in both windows
4. Verify video/audio streaming works
5. Test controls (mute, video off, screen share)

## Troubleshooting

### WebSocket Connection Fails
- Check if realtime-service is running on port 8087
- Check if API Gateway is running on port 8079
- Verify CORS configuration allows your frontend origin
- Check browser console for connection errors

### Video/Audio Not Working
- Grant camera/microphone permissions
- Check browser compatibility (Chrome/Firefox recommended)
- Verify getUserMedia API is working
- Check for HTTPS requirement in production

### ICE Connection Fails
- STUN servers are configured (Google's public STUN)
- For production, configure TURN servers
- Check firewall/NAT settings

### No Remote Stream
- Verify peer connection is established
- Check ontrack event handler
- Verify remote peer has media tracks enabled
- Check browser console for errors

## Production Considerations

### 1. TURN Server
Configure TURN server for NAT traversal:
```typescript
private iceServers: RTCIceServer[] = [
  { urls: 'stun:stun.l.google.com:19302' },
  {
    urls: 'turn:turn.example.com:3478',
    username: 'username',
    credential: 'password'
  }
];
```

### 2. HTTPS Required
WebRTC requires HTTPS in production for getUserMedia.

### 3. Database
Switch from H2 to PostgreSQL:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/realtimedb
    username: postgres
    password: password
```

### 4. Scalability
- Use Redis for distributed session management
- Configure RabbitMQ for message broker
- Deploy multiple instances behind load balancer
- Use sticky sessions for WebSocket connections

### 5. Security
- Implement JWT token validation
- Rate limiting on WebSocket connections
- Room access control
- End-to-end encryption for sensitive calls

## Performance Tips

1. **Limit video resolution** for better performance:
```typescript
await navigator.mediaDevices.getUserMedia({
  video: { width: 1280, height: 720 },
  audio: true
});
```

2. **Adaptive bitrate** based on network conditions

3. **Simulcast** for better quality adaptation

4. **Connection pooling** for WebSocket

## References

- [WebRTC API Documentation](https://developer.mozilla.org/en-US/docs/Web/API/WebRTC_API)
- [STOMP Protocol](https://stomp.github.io/)
- [Spring WebSocket Guide](https://spring.io/guides/gs/messaging-stomp-websocket)
- [SockJS](https://github.com/sockjs/sockjs-client)

---

**Last Updated**: December 14, 2025  
**Version**: 1.0.0  
**Status**: Production Ready ✅
