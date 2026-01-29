# Chat Service - Minimal CRUD cURL Commands

## Setup
```bash
# Set your token
TOKEN="YOUR_JWT_TOKEN_HERE"
BASE_URL="http://localhost:8079/api"
```

---

## ROOM CRUD

### 1. Create Room
```bash
curl -X POST "$BASE_URL/v1/rooms" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Study Group 101",
    "description": "General study discussion",
    "isPublic": true
  }'
```

### 2. Get Room Details
```bash
curl -X GET "$BASE_URL/v1/rooms/{roomId}" \
  -H "Authorization: Bearer $TOKEN"
```

### 3. Update Room
```bash
curl -X PUT "$BASE_URL/v1/rooms/{roomId}" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Study Group 101 - Updated",
    "description": "Updated description"
  }'
```

### 4. Get My Rooms
```bash
curl -X GET "$BASE_URL/v1/rooms/my-rooms" \
  -H "Authorization: Bearer $TOKEN"
```

### 5. Delete Room
```bash
curl -X DELETE "$BASE_URL/v1/rooms/{roomId}" \
  -H "Authorization: Bearer $TOKEN"
```

---

## MESSAGE CRUD

**Note:** Sending messages is done via WebSocket `/ws/chat`. HTTP endpoints are for read/update/delete only.

### 6. Get Message History
```bash
curl -X GET "$BASE_URL/v1/messages/rooms/{roomId}?page=0&size=50" \
  -H "Authorization: Bearer $TOKEN"
```

### 7. Edit Message
```bash
curl -X PUT "$BASE_URL/v1/messages/{messageId}" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Updated message content"
  }'
```

### 8. Delete Message
```bash
curl -X DELETE "$BASE_URL/v1/messages/{messageId}" \
  -H "Authorization: Bearer $TOKEN"
```

---

## PowerShell Version

Run the PowerShell script:
```powershell
.\chat-service-crud.ps1
```

Or Bash script:
```bash
bash chat-service-crud.sh
```

---

## Quick Test Workflow

1. **Create a room** → Get `roomId`
2. **Send message via WebSocket** (see WebSocket docs)
3. **Get message history** → Get `messageId`
4. **Edit message**
5. **Delete message**
6. **Delete room**

---

## Response Format

All responses follow `ApiResponse<T>` format:
```json
{
  "status": "success",
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2025-11-29T10:30:00"
}
```

---

## Ports

- **Via Gateway:** `http://localhost:8079/api/v1/...`
- **Direct:** `http://localhost:8083/api/v1/...`
