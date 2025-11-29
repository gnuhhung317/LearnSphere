package com.studyhub.chat_service.controller;

import com.studyhub.chat_service.dto.request.AddReactionRequest;
import com.studyhub.chat_service.dto.request.EditMessageRequest;
import com.studyhub.chat_service.dto.response.MessageResponse;
import com.studyhub.chat_service.service.MessageService;
import com.studyhub.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {
    
    private final MessageService messageService;
    
    // TODO: Replace with JWT authentication in production
    private static final Long MOCK_USER_ID = 1L;
    
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getMessageHistory(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        log.info("GET /api/v1/messages/rooms/{} - Getting message history", roomId);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<MessageResponse> response = messageService.getMessageHistory(roomId, MOCK_USER_ID, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PutMapping("/{messageId}")
    public ResponseEntity<ApiResponse<MessageResponse>> editMessage(
            @PathVariable Long messageId,
            @Valid @RequestBody EditMessageRequest request) {
        log.info("PUT /api/v1/messages/{} - Editing message", messageId);
        
        MessageResponse response = messageService.editMessage(messageId, request, MOCK_USER_ID);
        return ResponseEntity.ok(ApiResponse.success("Message edited successfully", response));
    }
    
    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(@PathVariable Long messageId) {
        log.info("DELETE /api/v1/messages/{} - Deleting message", messageId);
        
        messageService.deleteMessage(messageId, MOCK_USER_ID);
        return ResponseEntity.ok(ApiResponse.success("Message deleted successfully", null));
    }
    
    @PostMapping("/{messageId}/reactions")
    public ResponseEntity<ApiResponse<Void>> addReaction(
            @PathVariable Long messageId,
            @Valid @RequestBody AddReactionRequest request) {
        log.info("POST /api/v1/messages/{}/reactions - Adding reaction: {}", messageId, request.getEmoji());
        
        messageService.addReaction(messageId, request, MOCK_USER_ID);
        return ResponseEntity.ok(ApiResponse.success("Reaction added", null));
    }
    
    @DeleteMapping("/{messageId}/reactions/{emoji}")
    public ResponseEntity<ApiResponse<Void>> removeReaction(
            @PathVariable Long messageId,
            @PathVariable String emoji) {
        log.info("DELETE /api/v1/messages/{}/reactions/{} - Removing reaction", messageId, emoji);
        
        messageService.removeReaction(messageId, emoji, MOCK_USER_ID);
        return ResponseEntity.ok(ApiResponse.success("Reaction removed", null));
    }
    
    @PostMapping("/{messageId}/pin")
    public ResponseEntity<ApiResponse<MessageResponse>> pinMessage(@PathVariable Long messageId) {
        log.info("POST /api/v1/messages/{}/pin - Pinning message", messageId);
        
        MessageResponse response = messageService.pinMessage(messageId, MOCK_USER_ID);
        return ResponseEntity.ok(ApiResponse.success("Message pinned", response));
    }
    
    @DeleteMapping("/{messageId}/pin")
    public ResponseEntity<ApiResponse<Void>> unpinMessage(@PathVariable Long messageId) {
        log.info("DELETE /api/v1/messages/{}/pin - Unpinning message", messageId);
        
        messageService.unpinMessage(messageId, MOCK_USER_ID);
        return ResponseEntity.ok(ApiResponse.success("Message unpinned", null));
    }
    
    @GetMapping("/rooms/{roomId}/pinned")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getPinnedMessages(
            @PathVariable Long roomId) {
        log.info("GET /api/v1/messages/rooms/{}/pinned - Getting pinned messages", roomId);
        
        List<MessageResponse> response = messageService.getPinnedMessages(roomId, MOCK_USER_ID);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
