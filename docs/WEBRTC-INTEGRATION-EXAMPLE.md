# Integration Example: Adding Video Call to Dashboard

## Quick Integration

### Step 1: Add the Button to Dashboard

Edit your dashboard page (e.g., `app/dashboard/page.tsx`):

```tsx
import { VideoCallButton } from '@/components/video/VideoCallButton';

export default function DashboardPage() {
  return (
    <div className="p-6">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-3xl font-bold">Dashboard</h1>
        <VideoCallButton />
      </div>
      
      {/* Rest of your dashboard content */}
    </div>
  );
}
```

### Step 2: Add to Navigation Bar

Edit your layout navigation (e.g., `components/navigation.tsx`):

```tsx
import { VideoCallButton } from '@/components/video/VideoCallButton';
import { QuickVideoCallButton } from '@/components/video/VideoCallButton';

export function Navigation() {
  return (
    <nav className="flex items-center gap-4">
      <Link href="/dashboard">Dashboard</Link>
      <Link href="/workspace">Workspace</Link>
      <QuickVideoCallButton />
      {/* Other nav items */}
    </nav>
  );
}
```

### Step 3: Create a Study Room with Video

```tsx
'use client';

import { CustomRoomButton } from '@/components/video/VideoCallButton';

interface StudyRoomProps {
  roomId: string;
  name: string;
  members: number;
}

export function StudyRoomCard({ roomId, name, members }: StudyRoomProps) {
  return (
    <div className="border rounded-lg p-4">
      <h3 className="text-lg font-semibold">{name}</h3>
      <p className="text-sm text-gray-600">{members} members</p>
      
      <div className="mt-4 flex gap-2">
        <button className="btn-primary">Enter Room</button>
        <CustomRoomButton roomId={roomId}>
          Video Call
        </CustomRoomButton>
      </div>
    </div>
  );
}
```

### Step 4: Invite Users to Video Call

```tsx
'use client';

import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Copy, Check } from 'lucide-react';

export function InviteToVideoCall({ roomId }: { roomId: string }) {
  const [copied, setCopied] = useState(false);
  
  const inviteLink = `${window.location.origin}/video-call?room=${roomId}`;
  
  const copyToClipboard = async () => {
    await navigator.clipboard.writeText(inviteLink);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };
  
  return (
    <div className="space-y-2">
      <label className="text-sm font-medium">Invite Link</label>
      <div className="flex gap-2">
        <input
          type="text"
          value={inviteLink}
          readOnly
          className="flex-1 px-3 py-2 border rounded"
        />
        <Button onClick={copyToClipboard} variant="outline">
          {copied ? <Check className="h-4 w-4" /> : <Copy className="h-4 w-4" />}
        </Button>
      </div>
    </div>
  );
}
```

### Step 5: Show Active Calls

```tsx
'use client';

import { useEffect, useState } from 'react';
import { apiClient } from '@/lib/api-client';
import { CustomRoomButton } from '@/components/video/VideoCallButton';

interface ActiveCall {
  roomId: string;
  participantCount: number;
  participants: Array<{
    userId: number;
    username: string;
  }>;
}

export function ActiveCallsList() {
  const [calls, setCalls] = useState<ActiveCall[]>([]);
  
  useEffect(() => {
    // Fetch active calls from your API
    // This is just an example - you'd need to create an endpoint
    // that lists active rooms
    const fetchActiveCalls = async () => {
      try {
        // const response = await apiClient.get<ActiveCall[]>('/api/v1/realtime/active-rooms');
        // setCalls(response.data);
      } catch (error) {
        console.error('Failed to fetch active calls:', error);
      }
    };
    
    fetchActiveCalls();
    const interval = setInterval(fetchActiveCalls, 10000); // Refresh every 10s
    
    return () => clearInterval(interval);
  }, []);
  
  if (calls.length === 0) {
    return <p className="text-gray-500">No active calls</p>;
  }
  
  return (
    <div className="space-y-4">
      <h2 className="text-xl font-semibold">Active Video Calls</h2>
      {calls.map((call) => (
        <div key={call.roomId} className="border rounded-lg p-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="font-medium">Room: {call.roomId}</p>
              <p className="text-sm text-gray-600">
                {call.participantCount} participant{call.participantCount !== 1 && 's'}
              </p>
              <div className="mt-2 flex gap-2">
                {call.participants.slice(0, 3).map((p) => (
                  <span key={p.userId} className="text-xs bg-gray-100 px-2 py-1 rounded">
                    {p.username}
                  </span>
                ))}
                {call.participantCount > 3 && (
                  <span className="text-xs bg-gray-100 px-2 py-1 rounded">
                    +{call.participantCount - 3} more
                  </span>
                )}
              </div>
            </div>
            <CustomRoomButton roomId={call.roomId}>Join</CustomRoomButton>
          </div>
        </div>
      ))}
    </div>
  );
}
```

### Step 6: Add to Workspace Collaboration

```tsx
// In your workspace page
import { VideoCallButton } from '@/components/video/VideoCallButton';

export default function WorkspacePage({ params }: { params: { id: string } }) {
  const workspaceId = params.id;
  const videoRoomId = `workspace-${workspaceId}`;
  
  return (
    <div className="workspace">
      <header className="workspace-header">
        <h1>Workspace</h1>
        <div className="flex gap-2">
          <button>Chat</button>
          <CustomRoomButton roomId={videoRoomId}>
            Video Call
          </CustomRoomButton>
        </div>
      </header>
      
      {/* Workspace content */}
    </div>
  );
}
```

## Advanced: Custom Integration

### With useWebRTC Hook

```tsx
'use client';

import { useWebRTC } from '@/hooks/useWebRTC';
import { useAuth } from '@/contexts/auth-context';

export function CustomVideoInterface({ roomId }: { roomId: string }) {
  const { user } = useAuth();
  const {
    isConnected,
    isConnecting,
    localStream,
    participants,
    isAudioEnabled,
    isVideoEnabled,
    connect,
    disconnect,
    toggleAudio,
    toggleVideo,
    startScreenShare,
  } = useWebRTC(roomId, user!.id, user!.fullName);

  if (!isConnected) {
    return (
      <button onClick={() => connect(true, true)}>
        Join Video Call
      </button>
    );
  }

  return (
    <div className="custom-video-interface">
      {/* Your custom UI using the hook state */}
      <div>
        <p>{participants.length + 1} participants</p>
        <button onClick={toggleAudio}>
          {isAudioEnabled ? 'Mute' : 'Unmute'}
        </button>
        <button onClick={toggleVideo}>
          {isVideoEnabled ? 'Stop Video' : 'Start Video'}
        </button>
        <button onClick={startScreenShare}>Share Screen</button>
        <button onClick={disconnect}>Leave</button>
      </div>
    </div>
  );
}
```

## Testing Your Integration

1. **Start all services** (see WEBRTC-QUICKSTART.md)

2. **Navigate to your integrated page**

3. **Click the video call button**

4. **Verify**:
   - Camera/mic permissions requested
   - Video call page loads
   - Can join call
   - Can see video feed
   - Controls work

## Troubleshooting Integration

### "Cannot find module '@/components/video/VideoCallButton'"
```powershell
# Check file exists
ls studyhub/frontend/components/video/VideoCallButton.tsx
```

### "Module not found: Can't resolve '@/hooks/useWebRTC'"
```powershell
# Check file exists
ls studyhub/frontend/hooks/useWebRTC.ts
```

### Import errors
Make sure your `tsconfig.json` has:
```json
{
  "compilerOptions": {
    "paths": {
      "@/*": ["./*"]
    }
  }
}
```

## Next Steps

1. ✅ Add button to dashboard
2. ✅ Test video call works
3. 🔲 Add notification when someone joins
4. 🔲 Create room management UI
5. 🔲 Add scheduling for future calls
6. 🔲 Integrate with calendar
7. 🔲 Add call history

---

**Ready to go! Start integrating video calls into your app. 🎥**
