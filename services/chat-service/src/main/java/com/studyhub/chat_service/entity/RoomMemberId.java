package com.studyhub.chat_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomMemberId implements Serializable {
    
    @Column(name = "room_id")
    private Long roomId;
    
    @Column(name = "user_id")
    private String userId;
}
