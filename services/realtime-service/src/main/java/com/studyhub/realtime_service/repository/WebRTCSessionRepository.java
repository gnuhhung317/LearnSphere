package com.studyhub.realtime_service.repository;

import com.studyhub.realtime_service.entity.WebRTCSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebRTCSessionRepository extends JpaRepository<WebRTCSession, Long> {

    Optional<WebRTCSession> findBySessionId(String sessionId);

    List<WebRTCSession> findByRoomIdAndStatus(String roomId, WebRTCSession.SessionStatus status);

    List<WebRTCSession> findByUserIdAndStatus(Long userId, WebRTCSession.SessionStatus status);
}
