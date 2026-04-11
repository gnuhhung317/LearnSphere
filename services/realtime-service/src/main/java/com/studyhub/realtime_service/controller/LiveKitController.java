package com.studyhub.realtime_service.controller;

import com.studyhub.realtime_service.service.LiveKitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/realtime/livekit")
@RequiredArgsConstructor
public class LiveKitController {

    private final LiveKitService liveKitService;

    @GetMapping("/token")
    public ResponseEntity<Map<String, String>> getToken(
            @RequestParam String roomName,
            @RequestParam String identity,
            @RequestParam(required = false) String name) {

        String token = liveKitService.createAccessToken(roomName, identity, name != null ? name : identity);
        return ResponseEntity.ok(Map.of("token", token));
    }
}
