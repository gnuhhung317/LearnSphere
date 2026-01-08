package com.studyhub.chat_service.controller;

import com.studyhub.chat_service.dto.request.CreateChannelRequest;
import com.studyhub.chat_service.dto.request.UpdateChannelRequest;
import com.studyhub.chat_service.dto.response.ChannelResponse;
import com.studyhub.chat_service.service.ChannelService;
import com.studyhub.chat_service.util.JwtUtil;
import com.studyhub.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    @PostMapping
    public ResponseEntity<ApiResponse<ChannelResponse>> createChannel(
            @Valid @RequestBody CreateChannelRequest request) {
        String userId = JwtUtil.getUserIdFromJwt();
        log.info("POST /api/v1/channels - Creating channel: {} in room: {} by user: {}", 
            request.getName(), request.getRoomId(), userId);

        ChannelResponse response = channelService.createChannel(request, userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Channel created successfully", response));
    }

    @PutMapping("/{channelId}")
    public ResponseEntity<ApiResponse<ChannelResponse>> updateChannel(
            @PathVariable Long channelId,
            @Valid @RequestBody UpdateChannelRequest request) {
        String userId = JwtUtil.getUserIdFromJwt();
        log.info("PUT /api/v1/channels/{} - Updating channel by user: {}", channelId, userId);

        ChannelResponse response = channelService.updateChannel(channelId, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Channel updated successfully", response));
    }

    @DeleteMapping("/{channelId}")
    public ResponseEntity<ApiResponse<Void>> deleteChannel(@PathVariable Long channelId) {
        String userId = JwtUtil.getUserIdFromJwt();
        log.info("DELETE /api/v1/channels/{} - Deleting channel by user: {}", channelId, userId);

        channelService.deleteChannel(channelId, userId);
        return ResponseEntity.ok(ApiResponse.success("Channel deleted successfully", null));
    }

    @GetMapping("/{channelId}")
    public ResponseEntity<ApiResponse<ChannelResponse>> getChannelById(@PathVariable Long channelId) {
        String userId = JwtUtil.getUserIdFromJwt();
        log.info("GET /api/v1/channels/{} - Getting channel details for user: {}", channelId, userId);

        ChannelResponse response = channelService.getChannelById(channelId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse<List<ChannelResponse>>> getRoomChannels(@PathVariable Long roomId) {
        String userId = JwtUtil.getUserIdFromJwt();
        log.info("GET /api/v1/channels/room/{} - Getting channels for user: {}", roomId, userId);

        List<ChannelResponse> response = channelService.getRoomChannels(roomId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
