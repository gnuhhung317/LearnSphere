package com.studyhub.realtime_service.service;

import com.studyhub.realtime_service.dto.RoomInfo;
import com.studyhub.realtime_service.dto.RoomParticipant;
import com.studyhub.realtime_service.entity.WebRTCSession;
import com.studyhub.realtime_service.repository.WebRTCSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebRTCSessionService {

    private final WebRTCSessionRepository sessionRepository;

    @Transactional
    public WebRTCSession createSession(String roomId, Long userId, String username, String peerId) {
        log.info("Creating new session for room: {}, user: {}", roomId, userId);

        WebRTCSession session = WebRTCSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .roomId(roomId)
                .userId(userId)
                .username(username)
                .peerId(peerId)
                .status(WebRTCSession.SessionStatus.ACTIVE)
                .isAudioEnabled(true)
                .isVideoEnabled(true)
                .isScreenSharing(false)
                .build();

        return sessionRepository.save(session);
    }

    @Transactional
    public void endSession(String sessionId) {
        log.info("Ending session: {}", sessionId);
        sessionRepository.findBySessionId(sessionId).ifPresent(session -> {
            session.setStatus(WebRTCSession.SessionStatus.ENDED);
            session.setEndedAt(LocalDateTime.now());
            sessionRepository.save(session);
        });
    }

    @Transactional
    public void updateMediaState(String sessionId, Boolean audio, Boolean video, Boolean screen) {
        log.info("Updating media state for session: {}, audio: {}, video: {}, screen: {}", sessionId, audio, video,
                screen);
        sessionRepository.findBySessionId(sessionId).ifPresent(session -> {
            if (audio != null)
                session.setIsAudioEnabled(audio);
            if (video != null)
                session.setIsVideoEnabled(video);
            if (screen != null)
                session.setIsScreenSharing(screen);
            sessionRepository.save(session);
        });
    }

    public RoomInfo getRoomInfo(String roomId) {
        List<WebRTCSession> activeSessions = sessionRepository.findByRoomIdAndStatus(roomId,
                WebRTCSession.SessionStatus.ACTIVE);

        List<RoomParticipant> participants = activeSessions.stream()
                .map(s -> new RoomParticipant(
                        s.getSessionId(),
                        s.getUserId(),
                        s.getUsername(),
                        s.getPeerId(),
                        s.getIsAudioEnabled(),
                        s.getIsVideoEnabled(),
                        s.getIsScreenSharing()))
                .collect(Collectors.toList());

        return new RoomInfo(roomId, participants.size(), participants);
    }

    public List<WebRTCSession> getActiveSessionsForUser(Long userId) {
        return sessionRepository.findByUserIdAndStatus(userId, WebRTCSession.SessionStatus.ACTIVE);
    }

    @Transactional
    public void endAllSessionsForRoom(String roomId) {
        log.info("Ending all sessions for room: {}", roomId);
        List<WebRTCSession> activeSessions = sessionRepository.findByRoomIdAndStatus(roomId,
                WebRTCSession.SessionStatus.ACTIVE);
        activeSessions.forEach(session -> {
            session.setStatus(WebRTCSession.SessionStatus.ENDED);
            session.setEndedAt(LocalDateTime.now());
        });
        sessionRepository.saveAll(activeSessions);
    }
}
