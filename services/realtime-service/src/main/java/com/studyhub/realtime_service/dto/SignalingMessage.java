package com.studyhub.realtime_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignalingMessage {
    
    private String type;  // offer, answer, ice-candidate, join, leave
    private String roomId;
    private String sessionId;
    private Long userId;
    private String username;
    private String peerId;
    private Object payload;  // SDP or ICE candidate data
    private String targetPeerId;  // For directed messages
}
