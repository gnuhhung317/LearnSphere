package com.studyhub.chat_service.controller;

import com.studyhub.chat_service.dto.request.AddReactionRequest;
import com.studyhub.chat_service.dto.request.EditMessageRequest;
import com.studyhub.chat_service.dto.response.MessageResponse;
import com.studyhub.chat_service.service.MessageService;
import com.studyhub.chat_service.util.JwtUtil;
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

    @GetMapping("/channels/{channelId}")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getMessageHistory(
            @PathVariable Long channelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Long userId = JwtUtil.getUserIdFromJwt();
        log.info("GET /api/v1/messages/channels/{} - Getting history for user: {}", channelId, userId);

        Pageable pageable = PageRequest.of(page, size);
        Page<MessageResponse> response = messageService.getMessageHistoryByChannel(channelId, userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<ApiResponse<MessageResponse>> editMessage(
            @PathVariable Long messageId,
            @Valid @RequestBody EditMessageRequest request) {
        Long userId = JwtUtil.getUserIdFromJwt();
        log.info("PUT /api/v1/messages/{} - Editing by user: {}", messageId, userId);

        MessageResponse response = messageService.editMessage(messageId, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Message edited successfully", response));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(@PathVariable Long messageId) {
        Long userId = JwtUtil.getUserIdFromJwt();
        log.info("DELETE /api/v1/messages/{} - Deleting by user: {}", messageId, userId);

        messageService.deleteMessage(messageId, userId);
        return ResponseEntity.ok(ApiResponse.success("Message deleted successfully", null));
    }

    @PostMapping("/{messageId}/reactions")
    public ResponseEntity<ApiResponse<Void>> addReaction(
            @PathVariable Long messageId,
            @Valid @RequestBody AddReactionRequest request) {
        Long userId = JwtUtil.getUserIdFromJwt();
        log.info("POST /api/v1/messages/{}/reactions - User {} adding: {}", messageId, userId, request.getEmoji());

        messageService.addReaction(messageId, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Reaction added", null));
    }

    @DeleteMapping("/{messageId}/reactions/{emoji}")
    public ResponseEntity<ApiResponse<Void>> removeReaction(
            @PathVariable Long messageId,
            @PathVariable String emoji) {
        Long userId = JwtUtil.getUserIdFromJwt();
        log.info("DELETE /api/v1/messages/{}/reactions/{} - User {} removing", messageId, emoji, userId);

        messageService.removeReaction(messageId, emoji, userId);
        return ResponseEntity.ok(ApiResponse.success("Reaction removed", null));
    }

    @PostMapping("/{messageId}/pin")
    public ResponseEntity<ApiResponse<MessageResponse>> pinMessage(@PathVariable Long messageId) {
        Long userId = JwtUtil.getUserIdFromJwt();
        log.info("POST /api/v1/messages/{}/pin - Pinning by user: {}", messageId, userId);

        MessageResponse response = messageService.pinMessage(messageId, userId);
        return ResponseEntity.ok(ApiResponse.success("Message pinned", response));
    }

    @DeleteMapping("/{messageId}/pin")
    public ResponseEntity<ApiResponse<Void>> unpinMessage(@PathVariable Long messageId) {
        Long userId = JwtUtil.getUserIdFromJwt();
        log.info("DELETE /api/v1/messages/{}/pin - Unpinning by user: {}", messageId, userId);

        messageService.unpinMessage(messageId, userId);
        return ResponseEntity.ok(ApiResponse.success("Message unpinned", null));
    }

    @GetMapping("/rooms/{roomId}/pinned")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getPinnedMessages(
            @PathVariable Long roomId) {
        Long userId = JwtUtil.getUserIdFromJwt();
        log.info("GET /api/v1/messages/rooms/{}/pinned - Getting by user: {}", roomId, userId);

        List<MessageResponse> response = messageService.getPinnedMessages(roomId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
