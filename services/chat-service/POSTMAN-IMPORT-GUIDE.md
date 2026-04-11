# Chat Service - Postman Collection Import Guide

## Import vào Postman

### Cách 1: Import File
1. Mở Postman
2. Click **Import** (góc trên bên trái)
3. Chọn file: `Chat-Service-CRUD.postman_collection.json`
4. Click **Import**

### Cách 2: Drag & Drop
- Kéo file `Chat-Service-CRUD.postman_collection.json` vào Postman

---

## Setup Environment Variables

### Tạo Environment mới:
1. Click **Environments** (thanh bên trái)
2. Click **+** để tạo mới
3. Đặt tên: `Chat Service - Local`
4. Thêm variables:

| Variable | Initial Value | Current Value |
|----------|---------------|---------------|
| `base_url` | `http://localhost:8079/api` | `http://localhost:8079/api` |
| `jwt_token` | `YOUR_TOKEN_HERE` | `YOUR_TOKEN_HERE` |
| `roomId` | (auto-saved) | (auto-saved) |
| `messageId` | (auto-saved) | (auto-saved) |

5. Click **Save**

---

## Collection Structure

```
Chat Service - CRUD Tests
├── Room CRUD
│   ├── 1. Create Room          → Auto-saves roomId
│   ├── 2. Get Room Details
│   ├── 3. Update Room
│   ├── 4. Get My Rooms
│   ├── 5. Get Public Rooms
│   └── 6. Delete Room
│
├── Message CRUD
│   ├── 1. Get Message History  → Auto-saves messageId
│   ├── 2. Edit Message
│   └── 3. Delete Message
│
├── Room Members
│   ├── 1. Join Room
│   ├── 2. Get Room Members
│   └── 3. Leave Room
│
└── Message Reactions
    ├── 1. Add Reaction
    └── 2. Remove Reaction
```

---

## Auto-Chaining Features

✅ **roomId** auto-saved sau khi Create Room  
✅ **messageId** auto-saved sau khi Get Message History  
✅ **JWT token** dùng chung cho tất cả requests  
✅ Test assertions cho mỗi request  

---

## Usage Flow

### Basic Flow:
1. **Create Room** → `roomId` saved tự động
2. Get Room Details
3. Update Room
4. Get My Rooms
5. Delete Room

### Message Flow:
1. Create Room trước
2. Send message qua WebSocket (không có trong collection này)
3. **Get Message History** → `messageId` saved tự động
4. Edit Message
5. Delete Message

### Member Flow:
1. Create Room
2. Join Room
3. Get Room Members
4. Leave Room

---

## Notes

- **Sending messages:** Dùng WebSocket `/ws/chat` (không có HTTP POST endpoint)
- **Authentication:** Bearer token được set ở collection level
- **Base URL:** Default qua Gateway (`localhost:8079`)
- **Test assertions:** Tất cả requests đều có test scripts

---

## Run Collection

### Run toàn bộ:
1. Click **...** ở collection
2. Chọn **Run collection**
3. Select environment: `Chat Service - Local`
4. Click **Run Chat Service - CRUD Tests**

### Run từng folder:
- Right-click folder → **Run folder**

---

## Troubleshooting

**401 Unauthorized:**
- Check `jwt_token` trong environment
- Token có thể đã expired

**404 Not Found:**
- Check services có chạy không
- Check `roomId` / `messageId` có được save không

**503 Service Unavailable:**
- Chat Service chưa start
- Gateway không route được

---

## Endpoints Summary

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/v1/rooms` | Create room |
| GET | `/v1/rooms/{id}` | Get room details |
| PUT | `/v1/rooms/{id}` | Update room |
| DELETE | `/v1/rooms/{id}` | Delete room |
| GET | `/v1/rooms/my-rooms` | Get my rooms |
| GET | `/v1/rooms/public` | Get public rooms |
| POST | `/v1/rooms/{id}/join` | Join room |
| POST | `/v1/rooms/{id}/leave` | Leave room |
| GET | `/v1/rooms/{id}/members` | Get members |
| GET | `/v1/messages/rooms/{id}` | Get message history |
| PUT | `/v1/messages/{id}` | Edit message |
| DELETE | `/v1/messages/{id}` | Delete message |
| POST | `/v1/messages/{id}/reactions` | Add reaction |
| DELETE | `/v1/messages/{id}/reactions/{emoji}` | Remove reaction |
