package com.studyhub.chat_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "room_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomMember {
    
    @EmbeddedId
    private RoomMemberId id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roomId")
    @JoinColumn(name = "room_id")
    private Room room;
    
    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;
    
    @Column(name = "is_owner")
    private Boolean isOwner = false;
    
    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;
}
