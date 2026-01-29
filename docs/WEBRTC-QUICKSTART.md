# WebRTC Video Call - Quick Start Guide

## 🚀 Start the Services

### 1. Start Realtime Service
```powershell
cd d:\Code\Projects\learning-project\studyhub\services\realtime-service
mvn spring-boot:run
```

### 2. Start API Gateway (if not running)
```powershell
cd d:\Code\Projects\learning-project\studyhub\services\api-gateway
mvn spring-boot:run
```

### 3. Start Frontend (if not running)
```powershell
cd d:\Code\Projects\learning-project\studyhub\frontend
pnpm dev
```

## 📹 Test Video Call

### Option 1: Using the Video Call Page
1. Open browser: `http://localhost:3000/video-call?room=test-room`
2. Click "Join Call"
3. Allow camera and microphone permissions
4. Open same URL in another browser window/tab
5. Both users should see each other's video

### Option 2: Create Room Programmatically
```tsx
// In any component
import { useRouter } from 'next/navigation';

function YourComponent() {
  const router = useRouter();
  
  const startVideoCall = () => {
    const roomId = `room-${Date.now()}`;
    router.push(`/video-call?room=${roomId}`);
  };
  
  return <button onClick={startVideoCall}>Start Video Call</button>;
}
```

## 🎮 Controls

- **Microphone**: Toggle audio on/off
- **Camera**: Toggle video on/off
- **Screen Share**: Share your screen
- **End Call**: Leave the room

## 🔍 Verify It's Working

### Check Backend
```powershell
# Test health endpoint
curl http://localhost:8087/api/v1/realtime/health

# Check room info (after joining)
curl http://localhost:8087/api/v1/realtime/room/test-room
```

### Check Logs
- Backend logs: Console where `mvn spring-boot:run` is running
- Frontend logs: Browser Developer Console (F12)

### WebSocket Connection
Open browser console and check for:
```
✅ Connected to WebSocket
✅ Received signal: type=joined
✅ Received signal: type=participant-joined
```

## 🐛 Common Issues

### "Camera/Microphone permission denied"
- Grant permissions in browser settings
- On Windows: Check Windows Privacy settings

### "Failed to connect to signaling server"
- Verify realtime-service is running on port 8087
- Check API Gateway is running on port 8079

### "No video from remote user"
- Check both users have granted permissions
- Verify peer connection in browser console
- Check firewall settings

### WebSocket fails to connect
```powershell
# Check if service is listening
netstat -an | findstr 8087

# Should show:
# TCP    0.0.0.0:8087           0.0.0.0:0              LISTENING
```

## 📊 Monitor Sessions

### Using H2 Console (Dev)
```
URL: http://localhost:8087/h2-console
JDBC URL: jdbc:h2:mem:realtimedb
Username: sa
Password: (leave empty)

Query: SELECT * FROM webrtc_sessions;
```

## 🎯 Next Steps

1. **Add to Navigation**: Add video call button in your dashboard
2. **Room Management**: Create UI to list and join existing rooms
3. **Notifications**: Notify users when someone joins
4. **Chat Integration**: Add text chat during calls
5. **Recording**: Implement call recording feature

## 📖 Full Documentation

For complete implementation details, see:
- [WEBRTC-IMPLEMENTATION.md](./WEBRTC-IMPLEMENTATION.md)

---

✅ **Implementation Complete!**
