package com.studyhub.realtime_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomParticipant {
    
    private String sessionId;
    private Long userId;
    private String username;
    private String peerId;
    private Boolean isAudioEnabled;
    private Boolean isVideoEnabled;
    private Boolean isScreenSharing;
}
