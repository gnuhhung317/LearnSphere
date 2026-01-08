package com.studyhub.realtime_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "webrtc_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebRTCSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sessionId;

    @Column(nullable = false)
    private String roomId;

    @Column(nullable = false)
    private Long userId;

    private String username;

    private String peerId;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    private Boolean isAudioEnabled;

    private Boolean isVideoEnabled;

    private Boolean isScreenSharing;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime endedAt;

    public enum SessionStatus {
        ACTIVE,
        ENDED
    }
}
