package com.studyhub.realtime_service.service;

import com.studyhub.realtime_service.entity.WebRTCSession;
import com.studyhub.realtime_service.repository.WebRTCSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebRTCSessionServiceTest {

    @Mock
    private WebRTCSessionRepository sessionRepository;

    @InjectMocks
    private WebRTCSessionService sessionService;

    private WebRTCSession mockSession;

    @BeforeEach
    void setUp() {
        mockSession = new WebRTCSession();
        mockSession.setId(1L);
        mockSession.setSessionId("session-123");
        mockSession.setRoomId("room-123");
        mockSession.setUserId(1L);
        mockSession.setUsername("John Doe");
        mockSession.setPeerId("peer-123");
        mockSession.setStatus(WebRTCSession.SessionStatus.ACTIVE);
        mockSession.setIsAudioEnabled(true);
        mockSession.setIsVideoEnabled(true);
        mockSession.setIsScreenSharing(false);
    }

    @Test
    void testCreateSession() {
        // Arrange
        when(sessionRepository.save(any(WebRTCSession.class))).thenReturn(mockSession);

        // Act
        WebRTCSession result = sessionService.createSession("room-123", 1L, "John Doe", "peer-123");

        // Assert
        assertNotNull(result);
        assertEquals("room-123", result.getRoomId());
        assertEquals(1L, result.getUserId());
        assertEquals("John Doe", result.getUsername());
        assertEquals("peer-123", result.getPeerId());
        assertEquals(WebRTCSession.SessionStatus.ACTIVE, result.getStatus());
        verify(sessionRepository, times(1)).save(any(WebRTCSession.class));
    }

    @Test
    void testEndSession() {
        // Arrange
        when(sessionRepository.findBySessionId("session-123")).thenReturn(Optional.of(mockSession));

        // Act
        sessionService.endSession("session-123");

        // Assert
        assertEquals(WebRTCSession.SessionStatus.ENDED, mockSession.getStatus());
        assertNotNull(mockSession.getEndedAt());
        verify(sessionRepository, times(1)).save(mockSession);
    }

    @Test
    void testUpdateMediaState() {
        // Arrange
        when(sessionRepository.findBySessionId("session-123")).thenReturn(Optional.of(mockSession));

        // Act
        sessionService.updateMediaState("session-123", false, true, false);

        // Assert
        assertFalse(mockSession.getIsAudioEnabled());
        assertTrue(mockSession.getIsVideoEnabled());
        assertFalse(mockSession.getIsScreenSharing());
        verify(sessionRepository, times(1)).save(mockSession);
    }

    @Test
    void testGetRoomInfo() {
        // Arrange
        WebRTCSession session2 = new WebRTCSession();
        session2.setSessionId("session-456");
        session2.setRoomId("room-123");
        session2.setUserId(2L);
        session2.setUsername("Jane Smith");
        session2.setPeerId("peer-456");
        session2.setStatus(WebRTCSession.SessionStatus.ACTIVE);

        when(sessionRepository.findByRoomIdAndStatus("room-123", WebRTCSession.SessionStatus.ACTIVE))
                .thenReturn(Arrays.asList(mockSession, session2));

        // Act
        var roomInfo = sessionService.getRoomInfo("room-123");

        // Assert
        assertNotNull(roomInfo);
        assertEquals("room-123", roomInfo.getRoomId());
        assertEquals(2, roomInfo.getParticipantCount());
        assertEquals(2, roomInfo.getParticipants().size());
    }

    @Test
    void testGetActiveSessionsForUser() {
        // Arrange
        when(sessionRepository.findByUserIdAndStatus(1L, WebRTCSession.SessionStatus.ACTIVE))
                .thenReturn(List.of(mockSession));

        // Act
        List<WebRTCSession> result = sessionService.getActiveSessionsForUser(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockSession.getSessionId(), result.get(0).getSessionId());
    }

    @Test
    void testEndAllSessionsForRoom() {
        // Arrange
        WebRTCSession session2 = new WebRTCSession();
        session2.setSessionId("session-456");
        session2.setRoomId("room-123");
        session2.setStatus(WebRTCSession.SessionStatus.ACTIVE);

        when(sessionRepository.findByRoomIdAndStatus("room-123", WebRTCSession.SessionStatus.ACTIVE))
                .thenReturn(Arrays.asList(mockSession, session2));

        // Act
        sessionService.endAllSessionsForRoom("room-123");

        // Assert
        assertEquals(WebRTCSession.SessionStatus.ENDED, mockSession.getStatus());
        assertEquals(WebRTCSession.SessionStatus.ENDED, session2.getStatus());
        assertNotNull(mockSession.getEndedAt());
        assertNotNull(session2.getEndedAt());
        verify(sessionRepository, times(1)).saveAll(anyList());
    }
}
