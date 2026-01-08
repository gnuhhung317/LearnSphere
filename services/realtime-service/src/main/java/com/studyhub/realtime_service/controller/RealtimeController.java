package com.studyhub.realtime_service.controller;

import com.studyhub.realtime_service.dto.RoomInfo;
import com.studyhub.realtime_service.service.WebRTCSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/realtime")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RealtimeController {

    private final WebRTCSessionService sessionService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "realtime-service");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<RoomInfo> getRoomInfo(@PathVariable String roomId) {
        RoomInfo roomInfo = sessionService.getRoomInfo(roomId);
        return ResponseEntity.ok(roomInfo);
    }

    @DeleteMapping("/room/{roomId}")
    public ResponseEntity<Map<String, String>> endRoom(@PathVariable String roomId) {
        sessionService.endAllSessionsForRoom(roomId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Room ended successfully");
        response.put("roomId", roomId);
        return ResponseEntity.ok(response);
    }
}
