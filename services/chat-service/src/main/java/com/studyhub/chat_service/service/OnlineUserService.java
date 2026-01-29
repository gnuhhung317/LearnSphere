package com.studyhub.chat_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnlineUserService {

    private final SimpMessagingTemplate messagingTemplate;

    // userId -> Set<sessionId>
    private final Map<String, Set<String>> onlineUsers = new ConcurrentHashMap<>();

    public void addUser(String userId, String sessionId) {
        onlineUsers.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        broadcastOnlineCount();
    }

    public void removeUser(String userId, String sessionId) {
        Set<String> sessions = onlineUsers.get(userId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                onlineUsers.remove(userId);
            }
        }
        broadcastOnlineCount();
    }

    public int getOnlineCount() {
        return onlineUsers.size();
    }

    public boolean isUserOnline(String userId) {
        return onlineUsers.containsKey(userId);
    }

    private void broadcastOnlineCount() {
        try {
            // Simplified: just broadcasting the count for now
            messagingTemplate.convertAndSend("/topic/presence", Map.of("count", onlineUsers.size()));
        } catch (Exception e) {
            log.error("Failed to broadcast online count", e);
        }
    }
}
