package com.studyhub.chat_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "creator_id", nullable = false)
    private String creatorId;

    @Column(name = "is_public")
    private Boolean isPublic = true;

//    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false)
    private String roomType = RoomType.GROUP.toString();

    @Column(name = "invite_code", unique = true)
    private String inviteCode;

    @Column(name = "max_members")
    private Integer maxMembers = 50;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Relationships
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Channel> channels = new ArrayList<>();

}
