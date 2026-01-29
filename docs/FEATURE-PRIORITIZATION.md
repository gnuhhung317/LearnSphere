# Feature Prioritization Analysis
**Date:** December 13, 2025  
**Status:** Implementation Plan

## Phân loại độ khó & độ khả thi

### 🟢 **Tier 1: Dễ - Khả thi cao (Backend đã có API)**
Backend APIs đã triển khai xong, chỉ cần implement frontend

#### 1. Message Features (Backend APIs: ✅ Đầy đủ)
- **Edit message** - API: `PUT /api/v1/messages/{messageId}` ✅
- **Delete message** - API: `DELETE /api/v1/messages/{messageId}` ✅
- **Message reactions** - API: `POST/DELETE /api/v1/messages/{messageId}/reactions` ✅
- **Pin message** - API: `POST/DELETE /api/v1/messages/{messageId}/pin` ✅
- **Message pagination** - API: `GET /api/v1/messages/channels/{channelId}?page=0&size=50` ✅

**Độ phức tạp:** ⭐⭐ (Chỉ cần UI components & state management)  
**Thời gian ước tính:** 3-4 giờ

#### 2. Room Settings (Backend APIs: ✅ Đầy đủ)
- **Leave room** - API: `POST /api/v1/rooms/{roomId}/leave` ✅
- **Delete room** - API: `DELETE /api/v1/rooms/{roomId}` (owner only) ✅

**Độ phức tạp:** ⭐ (Modal confirmations)  
**Thời gian ước tính:** 1 giờ

---

### 🟡 **Tier 2: Trung bình - Khả thi khá cao**
Cần implement cả backend và frontend, nhưng logic đơn giản

#### 3. Channel Management (Backend APIs: ⚠️ Cần bổ sung)
- **Create channel** - API: `POST /api/v1/rooms/{roomId}/channels` ❌
- **Delete channel** - API: `DELETE /api/v1/channels/{channelId}` ❌
- **Rename channel** - API: `PUT /api/v1/channels/{channelId}` ❌
- **Channel permissions** - Cần thiết kế permission system ❌

**Độ phức tạp:** ⭐⭐⭐ (Backend entity relationships, frontend modals)  
**Thời gian ước tính:** 4-6 giờ

#### 4. Member Management (Backend APIs: ⚠️ Một phần)
- **Invite members** - API: `POST /api/v1/rooms/{roomId}/invite` ✅
- **Remove members** - API: `DELETE /api/v1/rooms/{roomId}/members/{userId}` ✅
- **Member search** - Frontend filter, không cần API mới ✅
- **Change roles** - Cần permission system ❌

**Độ phức tạp:** ⭐⭐⭐  
**Thời gian ước tính:** 3-4 giờ

#### 5. Reply/Thread (Backend support: ✅ Có `parentMessageId`)
- **Reply UI** - Entity `Message.parentMessageId` đã có ✅
- **Thread view** - Cần API: `GET /api/v1/messages/{messageId}/replies` ❌

**Độ phức tạp:** ⭐⭐⭐⭐  
**Thời gian ước tính:** 4-5 giờ

---

### 🟠 **Tier 3: Khó - Khả thi trung bình**
Cần tích hợp với service khác hoặc logic phức tạp

#### 6. File Upload & Media (Integration với Media Service)
- **Upload files** - Cần tích hợp `media-service` ❌
- **File browser** - API: `GET /api/v1/media/files?roomId={roomId}` ❌
- **File preview** - Frontend modal + download API ❌
- **Download files** - API: `GET /api/v1/media/files/{fileId}/download` ❌
- **AI processing status** - WebSocket event từ AI Service ❌

**Độ phức tạp:** ⭐⭐⭐⭐⭐ (Multi-service, file handling, progress tracking)  
**Thời gian ước tính:** 8-10 giờ

#### 7. AI Assistant Panel (Integration với AI Service)
- **Summarize conversation** - API: `POST /api/v1/ai/summarize` ❌
- **Generate notes** - API: `POST /api/v1/ai/generate-notes` ❌
- **Extract action items** - API: `POST /api/v1/ai/extract-actions` ❌
- **AI chat** - WebSocket `/topic/ai-assistant` ❌

**Độ phức tạp:** ⭐⭐⭐⭐⭐ (AI service integration, streaming responses)  
**Thời gian ước tính:** 10-12 giờ

---

### 🔴 **Tier 4: Rất khó - Khả thi thấp**
Cần infrastructure mới, WebRTC signaling, real-time sync phức tạp

#### 8. Video Call (WebRTC - Cần Realtime Service)
- **Start/Join call** - WebRTC signaling API ❌
- **Video streams** - Media stream handling ❌
- **Audio controls** - WebRTC track control ❌
- **Screen sharing** - `getDisplayMedia()` API ❌
- **Call signaling** - Realtime Service integration ❌

**Độ phức tạp:** ⭐⭐⭐⭐⭐⭐⭐⭐ (WebRTC, STUN/TURN servers, signaling)  
**Thời gian ước tính:** 20+ giờ

#### 9. Real-time Features (WebSocket Advanced)
- **WebSocket reconnection** - Tự động reconnect logic ⚠️
- **Message delivery status** - Read receipts system ❌
- **Presence system** - Heartbeat + WebSocket ❌
- **Read receipts** - Tracking table + WebSocket broadcast ❌

**Độ phức tạp:** ⭐⭐⭐⭐⭐⭐ (Distributed state, scalability)  
**Thời gian ước tính:** 12-15 giờ

---

### 🟣 **Tier 5: UI/UX Improvements - Khả thi cao nhưng ưu tiên thấp**

#### 10. UI Enhancements (Không cần backend mới)
- **Emoji picker** - Frontend component library ✅
- **@mentions autocomplete** - Regex + member list ✅
- **Message search** - Frontend filter hoặc Elasticsearch integration ⚠️
- **Keyboard shortcuts** - Event listeners ✅
- **Dark/Light theme toggle** - Tailwind theme switching ✅
- **Notification sounds** - HTML5 Audio API ✅

**Độ phức tạp:** ⭐⭐  
**Thời gian ước tính:** 4-6 giờ

---

## 🎯 **Recommended Implementation Order**

### Sprint 1 (Core Features - 8h)
1. ✅ **Message edit/delete** (2h)
2. ✅ **Message reactions** (2h)
3. ✅ **Pin message** (1h)
4. ✅ **Leave/Delete room** (1h)
5. ✅ **Message pagination** (2h)

### Sprint 2 (Channel & Members - 10h)
6. 🔨 **Channel CRUD operations** (6h)
7. 🔨 **Member management UI** (2h)
8. 🔨 **Reply/Thread UI** (2h)

### Sprint 3 (UI Polish - 6h)
9. 🔨 **Emoji picker** (2h)
10. 🔨 **@mentions** (2h)
11. 🔨 **Keyboard shortcuts** (1h)
12. 🔨 **Theme toggle** (1h)

### Sprint 4+ (Advanced - 30h+)
13. 🔨 **File upload & media integration** (10h)
14. 🔨 **AI Assistant features** (12h)
15. 🔨 **WebRTC video calls** (20h+)
16. 🔨 **Presence & read receipts** (15h)

---

## ✅ **Implementation Status**

| Feature | Backend | Frontend | Status |
|---------|---------|----------|--------|
| Edit message | ✅ | ❌ | Ready to implement |
| Delete message | ✅ | ❌ | Ready to implement |
| Reactions | ✅ | ❌ | Ready to implement |
| Pin message | ✅ | ❌ | Ready to implement |
| Leave room | ✅ | ❌ | Ready to implement |
| Channel CRUD | ❌ | ❌ | Need backend API |
| Reply/Thread | ⚠️ | ❌ | Need replies API |
| File upload | ❌ | ❌ | Need media service |
| Video call | ❌ | ❌ | Need realtime service |

---

## 📊 **Backend API Coverage**

### Chat Service (Port 8083)
- ✅ Message CRUD
- ✅ Reactions
- ✅ Pin/Unpin
- ✅ Room management
- ✅ Member management
- ❌ Channel CRUD (cần thêm)
- ❌ Message replies endpoint

### Media Service (Port 8084)
- ❌ File upload
- ❌ Transcoding status
- ❌ Download endpoint

### AI Service (Port 8085)
- ❌ Summarization
- ❌ Note generation
- ❌ Action extraction

### Realtime Service (Port 8087)
- ❌ WebRTC signaling
- ❌ Call management

---

## 🚀 **Next Steps**

Tôi sẽ bắt đầu implement **Tier 1 features** ngay bây giờ vì:
1. Backend APIs đã sẵn sàng
2. Không cần microservice mới
3. Tác động cao đến UX
4. Thời gian triển khai nhanh (3-4 giờ)

**Starting with:** Message actions (edit, delete, reactions, pin)
