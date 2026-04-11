package com.studyhub.realtime_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomInfo {
    
    private String roomId;
    private Integer participantCount;
    private List<RoomParticipant> participants;
}
