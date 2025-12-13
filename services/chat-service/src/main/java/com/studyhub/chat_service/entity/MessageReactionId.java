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
public class MessageReactionId implements Serializable {
    
    @Column(name = "message_id")
    private Long messageId;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "emoji")
    private String emoji;
}
