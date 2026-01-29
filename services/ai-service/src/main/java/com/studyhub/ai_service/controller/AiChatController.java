package com.studyhub.ai_service.controller;

import com.studyhub.ai_service.entity.AIMessage;
import com.studyhub.ai_service.entity.AISession;
import com.studyhub.ai_service.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final ChatbotService chatbotService;
    private final com.studyhub.ai_service.service.LearningAssistantService learningAssistantService;

    @GetMapping("/learning-space/{id}/map")
    public ResponseEntity<Map<String, String>> getLearningMap(@PathVariable Long id) {
        String mermaidCode = learningAssistantService.generateLearningMap(id);
        return ResponseEntity.ok(Map.of("mermaid", mermaidCode));
    }

    // --- Session Management Endpoints ---

    @PostMapping("/sessions")
    public ResponseEntity<AISession> createSession(@RequestBody Map<String, Object> request) {
        // TODO: Get userId from Security Context. For now assuming passed or hardcoded
        Long userId = request.containsKey("userId") ? Long.valueOf(request.get("userId").toString()) : 1L;
        Long learningSpaceId = Long.valueOf(request.get("learningSpaceId").toString());
        String mode = request.getOrDefault("mode", "RAG").toString();

        return ResponseEntity.ok(chatbotService.createSession(userId, learningSpaceId, mode));
    }

    @GetMapping("/sessions/space/{spaceId}")
    public ResponseEntity<List<AISession>> getSessionsBySpace(@PathVariable Long spaceId,
            @RequestParam(defaultValue = "1") Long userId) {
        return ResponseEntity.ok(chatbotService.getSessions(userId, spaceId));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<List<Map<String, String>>> getSessionMessages(@PathVariable Long sessionId) {
        List<AIMessage> messages = chatbotService.getSessionMessages(sessionId);
        List<Map<String, String>> response = messages.stream()
                .map(msg -> Map.of(
                        "role", msg.getRole().name().toLowerCase(),
                        "content", msg.getContent(),
                        "createdAt", msg.getCreatedAt().toString()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sessions/{sessionId}/chat")
    public ResponseEntity<Map<String, String>> chatSession(@PathVariable Long sessionId,
            @RequestBody Map<String, String> request) {
        String query = request.get("query");
        String response = chatbotService.askSession(sessionId, query);
        return ResponseEntity.ok(Map.of("message", response));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long sessionId,
            @RequestParam(defaultValue = "1") Long userId) {
        chatbotService.deleteSession(sessionId, userId);
        return ResponseEntity.noContent().build();
    }

    // --- Legacy / Stateless Endpoints (Keeping for compatibility if needed) ---

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, Object> request) {
        Long roomId = Long.valueOf(request.get("roomId").toString());
        Long channelId = request.containsKey("channelId") ? Long.valueOf(request.get("channelId").toString()) : null;
        String query = request.get("query").toString();

        String response = chatbotService.ask(roomId, channelId, query);
        return Map.of("response", response);
    }

    @PostMapping("/learning-space/chat")
    public Map<String, String> chatLearningSpace(@RequestBody Map<String, Object> request) {
        Long learningSpaceId = Long.valueOf(request.get("learningSpaceId").toString());
        String query = request.get("query").toString();
        // Redirecting to simple stateless for now, but client should move to
        // session-based
        String response = chatbotService.askLearningSpace(learningSpaceId, query);
        return Map.of("response", response);
    }

    @PostMapping("/chat/resource")
    public ResponseEntity<Map<String, String>> chatWithResource(@RequestBody Map<String, String> request) {
        String fileId = request.get("fileId");
        String message = request.get("message");
        String response = chatbotService.chatWithFile(fileId, message);
        return ResponseEntity.ok(Map.of("message", response));
    }
}
