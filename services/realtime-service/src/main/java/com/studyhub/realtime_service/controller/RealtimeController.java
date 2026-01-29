package com.studyhub.realtime_service.controller;

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

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "realtime-service");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}
