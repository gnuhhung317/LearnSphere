package com.studyhub.realtime_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallEvent {
    public enum Type {
        START, END, JOIN, LEAVE
    }

    private Type type;
    private String roomId;
    private String participantId;
    private String participantName;
    private String timestamp;
}
