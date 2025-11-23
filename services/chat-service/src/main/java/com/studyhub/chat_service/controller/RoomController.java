package com.studyhub.chat_service.controller;

import com.studyhub.chat_service.dto.request.CreateRoomRequest;
import com.studyhub.chat_service.dto.request.InviteMemberRequest;
import com.studyhub.chat_service.dto.request.JoinRoomRequest;
import com.studyhub.chat_service.dto.request.UpdateRoomRequest;
import com.studyhub.chat_service.dto.response.MemberResponse;
import com.studyhub.chat_service.dto.response.RoomResponse;
import com.studyhub.chat_service.dto.response.RoomSummary;
import com.studyhub.chat_service.service.RoomService;
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
    
    // TODO: Replace with JWT authentication in production
    private static final Long MOCK_USER_ID = 1L;
    
    @PostMapping
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(
            @Valid @RequestBody CreateRoomRequest request) {
        log.info("POST /api/v1/rooms - Creating room: {}", request.getName());
        
        RoomResponse response = roomService.createRoom(request, MOCK_USER_ID);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Room created successfully", response));
    }
    
    @PutMapping("/{roomId}")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoom(
            @PathVariable Long roomId,
            @Valid @RequestBody UpdateRoomRequest request) {
        log.info("PUT /api/v1/rooms/{} - Updating room", roomId);
        
        RoomResponse response = roomService.updateRoom(roomId, request, MOCK_USER_ID);
        return ResponseEntity.ok(ApiResponse.success("Room updated successfully", response));
    }
    
    @DeleteMapping("/{roomId}")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable Long roomId) {
        log.info("DELETE /api/v1/rooms/{} - Deleting room", roomId);
        
        roomService.deleteRoom(roomId, MOCK_USER_ID);
        return ResponseEntity.ok(ApiResponse.success("Room deleted successfully", null));
    }
    
    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomById(@PathVariable Long roomId) {
        log.info("GET /api/v1/rooms/{} - Getting room details", roomId);
        
        RoomResponse response = roomService.getRoomById(roomId, MOCK_USER_ID);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/my-rooms")
    public ResponseEntity<ApiResponse<List<RoomSummary>>> getUserRooms() {
        log.info("GET /api/v1/rooms/my-rooms - Getting user's rooms");
        
        List<RoomSummary> response = roomService.getUserRooms(MOCK_USER_ID);
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
        log.info("POST /api/v1/rooms/{}/join - User joining room", roomId);
        
        RoomResponse response = roomService.joinRoom(roomId, request, MOCK_USER_ID);
        return ResponseEntity.ok(ApiResponse.success("Joined room successfully", response));
    }
    
    @PostMapping("/{roomId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveRoom(@PathVariable Long roomId) {
        log.info("POST /api/v1/rooms/{}/leave - User leaving room", roomId);
        
        roomService.leaveRoom(roomId, MOCK_USER_ID);
        return ResponseEntity.ok(ApiResponse.success("Left room successfully", null));
    }
    
    @PostMapping("/{roomId}/invite")
    public ResponseEntity<ApiResponse<Void>> inviteMember(
            @PathVariable Long roomId,
            @Valid @RequestBody InviteMemberRequest request) {
        log.info("POST /api/v1/rooms/{}/invite - Inviting user: {}", roomId, request.getUserId());
        
        roomService.inviteMember(roomId, request, MOCK_USER_ID);
        return ResponseEntity.ok(ApiResponse.success("User invited successfully", null));
    }
    
    @DeleteMapping("/{roomId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long roomId,
            @PathVariable Long userId) {
        log.info("DELETE /api/v1/rooms/{}/members/{} - Removing member", roomId, userId);
        
        roomService.removeMember(roomId, userId, MOCK_USER_ID);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully", null));
    }
    
    @GetMapping("/{roomId}/members")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> getRoomMembers(
            @PathVariable Long roomId) {
        log.info("GET /api/v1/rooms/{}/members - Getting room members", roomId);
        
        List<MemberResponse> response = roomService.getRoomMembers(roomId, MOCK_USER_ID);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
