package com.studyhub.chat_service.controller;

import com.studyhub.chat_service.dto.request.CreateDirectMessageRequest;
import com.studyhub.chat_service.dto.request.CreateRoomRequest;
import com.studyhub.chat_service.dto.request.InviteMemberRequest;
import com.studyhub.chat_service.dto.request.JoinRoomRequest;
import com.studyhub.chat_service.dto.request.UpdateRoomRequest;
import com.studyhub.chat_service.dto.response.MemberResponse;
import com.studyhub.chat_service.dto.response.RoomResponse;
import com.studyhub.chat_service.dto.response.RoomSummary;
import com.studyhub.chat_service.service.RoomService;
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
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(
            @Valid @RequestBody CreateRoomRequest request) {
        Long userId = JwtUtil.getUserIdFromJwt();
        log.info("POST /api/v1/rooms - Creating room: {} by user: {}", request.getName(), userId);

        RoomResponse response = roomService.createRoom(request, userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Room created successfully", response));
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoom(
            @PathVariable Long roomId,
            @Valid @RequestBody UpdateRoomRequest request) {
        Long userId = JwtUtil.getUserIdFromJwt();
        log.info("PUT /api/v1/rooms/{} - Updating room by user: {}", roomId, userId);

        RoomResponse response = roomService.updateRoom(roomId, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Room updated successfully", response));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable Long roomId) {
        Long userId = JwtUtil.getUserIdFromJwt();
        log.info("DELETE /api/v1/rooms/{} - Deleting room by user: {}", roomId, userId);

        roomService.deleteRoom(roomId, userId);
        return ResponseEntity.ok(ApiResponse.success("Room deleted successfully", null));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomById(@PathVariable Long roomId) {
        Long userId = JwtUtil.getUserIdFromJwt();
        log.info("GET /api/v1/rooms/{} - Getting room details for user: {}", roomId, userId);

        RoomResponse response = roomService.getRoomById(roomId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my-rooms")
    public ResponseEntity<ApiResponse<List<RoomSummary>>> getUserRooms() {
        Long userId = JwtUtil.getUserIdFromJwt();
        log.info("GET /api/v1/rooms/my-rooms - Getting rooms for user: {}", userId);

        List<RoomSummary> response = roomService.getUserRooms(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<RoomSummary>>> getPublicRooms() {
        log.info("GET /api/v1/rooms/public - Getting public rooms");

        List<RoomSummary> response = roomService.getPublicRooms();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{roomId}/join")
    public ResponseEntity<ApiResponse<RoomResponse>> joinRoom(
            @PathVariable Long roomId,
            @Valid @RequestBody JoinRoomRequest request) {
        Long userId = JwtUtil.getUserIdFromJwt();
        log.info("POST /api/v1/rooms/{}/join - User {} joining room", roomId, userId);

        RoomResponse response = roomService.joinRoom(roomId, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Joined room successfully", response));
    }

    @PostMapping("/{roomId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveRoom(@PathVariable Long roomId) {
        Long userId = JwtUtil.getUserIdFromJwt();
        log.info("POST /api/v1/rooms/{}/leave - User {} leaving room", roomId, userId);

        roomService.leaveRoom(roomId, userId);
        return ResponseEntity.ok(ApiResponse.success("Left room successfully", null));
    }

    @PostMapping("/{roomId}/invite")
    public ResponseEntity<ApiResponse<Void>> inviteMember(
            @PathVariable Long roomId,
            @Valid @RequestBody InviteMemberRequest request) {
        Long userId = JwtUtil.getUserIdFromJwt();
        log.info("POST /api/v1/rooms/{}/invite - User {} inviting: {}", roomId, userId, request.getUserId());

        roomService.inviteMember(roomId, request, userId);
        return ResponseEntity.ok(ApiResponse.success("User invited successfully", null));
    }

    @DeleteMapping("/{roomId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long roomId,
            @PathVariable Long userId) {
        Long currentUserId = JwtUtil.getUserIdFromJwt();
        log.info("DELETE /api/v1/rooms/{}/members/{} - Removing member by user: {}", roomId, userId, currentUserId);

        roomService.removeMember(roomId, userId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully", null));
    }

    @GetMapping("/{roomId}/members")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> getRoomMembers(
            @PathVariable Long roomId) {
        Long userId = JwtUtil.getUserIdFromJwt();
        log.info("GET /api/v1/rooms/{}/members - Getting members for user: {}", roomId, userId);

        List<MemberResponse> response = roomService.getRoomMembers(roomId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ========== DIRECT MESSAGE ENDPOINTS ==========
    /**
     * Create or get existing Direct Message conversation with another user POST
     * /api/v1/rooms/direct-messages
     *
     * Request body: { "recipientUserId": 2 }
     *
     * Returns existing DM room if already exists, or creates new one
     */
    @PostMapping("/direct-messages")
    public ResponseEntity<ApiResponse<RoomResponse>> createDirectMessage(
            @Valid @RequestBody CreateDirectMessageRequest request) {
        Long userId = JwtUtil.getUserIdFromJwt();
        log.info("POST /api/v1/rooms/direct-messages - User {} creating DM with: {}",
                userId, request.getRecipientUserId());

        RoomResponse response = roomService.createOrGetDirectMessage(request, userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Direct message created", response));
    }

    /**
     * Get all Direct Message conversations for current user GET
     * /api/v1/rooms/direct-messages
     */
    @GetMapping("/direct-messages")
    public ResponseEntity<ApiResponse<List<RoomSummary>>> getUserDirectMessages() {
        Long userId = JwtUtil.getUserIdFromJwt();
        log.info("GET /api/v1/rooms/direct-messages - Getting DMs for user: {}", userId);

        List<RoomSummary> response = roomService.getUserDirectMessages(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
