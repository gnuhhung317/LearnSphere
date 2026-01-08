package com.studyhub.ai_service.controller;

import com.studyhub.ai_service.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final ChatbotService chatbotService;

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, Object> request) {
        Long roomId = Long.valueOf(request.get("roomId").toString());
        String query = request.get("query").toString();

        String response = chatbotService.ask(roomId, query);
        return Map.of("response", response);
    }
}
