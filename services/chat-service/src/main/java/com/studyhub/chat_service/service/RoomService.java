package com.studyhub.chat_service.service;

import com.studyhub.chat_service.dto.request.CreateDirectMessageRequest;
import com.studyhub.chat_service.dto.request.CreateRoomRequest;
import com.studyhub.chat_service.dto.request.InviteMemberRequest;
import com.studyhub.chat_service.dto.request.JoinRoomRequest;
import com.studyhub.chat_service.dto.request.UpdateRoomRequest;
import com.studyhub.chat_service.dto.response.MemberResponse;
import com.studyhub.chat_service.dto.response.RoomResponse;
import com.studyhub.chat_service.dto.response.RoomSummary;
import com.studyhub.chat_service.entity.Channel;
import com.studyhub.chat_service.entity.Room;
import com.studyhub.chat_service.entity.RoomMember;
import com.studyhub.chat_service.entity.RoomMemberId;
import com.studyhub.chat_service.entity.RoomType;
import com.studyhub.chat_service.exception.RoomFullException;
import com.studyhub.chat_service.exception.RoomNotFoundException;
import com.studyhub.chat_service.exception.UnauthorizedException;
import com.studyhub.chat_service.repository.RoomMemberRepository;
import com.studyhub.chat_service.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private static final int MAX_MEMBERS = 50;

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public RoomResponse createRoom(CreateRoomRequest request, Long currentUserId) {
        log.info("Creating room: {} by user: {}", request.getName(), currentUserId);

        Room room = Room.builder()
                .name(request.getName())
                .description(request.getDescription())
                .creatorId(currentUserId)
                .isPublic(request.getIsPublic())
                .roomType(RoomType.GROUP.toString())
                .maxMembers(MAX_MEMBERS)
                .build();

        // Generate invite code for private rooms
        if (!request.getIsPublic()) {
            room.setInviteCode(generateInviteCode());
        }

        Room savedRoom = roomRepository.save(room);

        // Create default "General" channel
        Channel generalChannel = Channel.builder()
                .name("General")
                .room(savedRoom) // Set bidirectional relationship
                .build();
        savedRoom.getChannels().add(generalChannel);

        // Add creator as owner
        RoomMember creatorMember = RoomMember.builder()
                .id(new RoomMemberId(savedRoom.getId(), currentUserId))
                .room(savedRoom)
                .isOwner(true)
                .build();
        roomMemberRepository.save(creatorMember);

        // Save again to persist channel (cascade will save the channel)
        savedRoom = roomRepository.save(savedRoom);

        return toRoomResponse(savedRoom, currentUserId);
    }

    @Transactional
    public RoomResponse updateRoom(Long roomId, UpdateRoomRequest request, Long currentUserId) {
        log.info("Updating room: {} by user: {}", roomId, currentUserId);

        Room room = getRoomOrThrow(roomId);
        validateOwnership(roomId, currentUserId);

        if (request.getName() != null) {
            room.setName(request.getName());
        }
        if (request.getDescription() != null) {
            room.setDescription(request.getDescription());
        }
        if (request.getIsPublic() != null) {
            room.setIsPublic(request.getIsPublic());
            // Generate or remove invite code based on visibility
            if (!request.getIsPublic() && room.getInviteCode() == null) {
                room.setInviteCode(generateInviteCode());
            }
        }

        Room updatedRoom = roomRepository.save(room);
        return toRoomResponse(updatedRoom, currentUserId);
    }

    @Transactional
    public void deleteRoom(Long roomId, Long currentUserId) {
        log.info("Deleting room: {} by user: {}", roomId, currentUserId);

        Room room = getRoomOrThrow(roomId);
        validateOwnership(roomId, currentUserId);

        roomRepository.delete(room);
    }

    @Transactional(readOnly = true)
    public RoomResponse getRoomById(Long roomId, Long currentUserId) {
        Room room = getRoomOrThrow(roomId);

        // Check if user is member for private rooms
        if (!room.getIsPublic()) {
            validateMembership(roomId, currentUserId);
        }

        return toRoomResponse(room, currentUserId);
    }

    @Transactional(readOnly = true)
    public List<RoomSummary> getUserRooms(Long userId) {
        log.info("Getting rooms for user: {}", userId);

        return roomRepository.findRoomsByUserId(userId).stream()
                .map(this::toRoomSummary)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RoomSummary> getPublicRooms() {
        log.info("Getting public rooms");

        return roomRepository.findPublicRooms().stream()
                .map(this::toRoomSummary)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomResponse joinRoom(Long roomId, JoinRoomRequest request, Long currentUserId) {
        log.info("User: {} joining room: {}", currentUserId, roomId);

        Room room = getRoomOrThrow(roomId);

        // Check if already member
        if (roomRepository.existsMemberInRoom(roomId, currentUserId)) {
            throw new IllegalStateException("User is already a member of this room");
        }

        // Validate invite code for private rooms
        if (!room.getIsPublic()) {
            if (request.getInviteCode() == null
                    || !request.getInviteCode().equals(room.getInviteCode())) {
                throw new UnauthorizedException("Invalid invite code");
            }
        }

        // Check room capacity
        long memberCount = roomMemberRepository.countByRoomId(roomId);
        if (memberCount >= room.getMaxMembers()) {
            throw new RoomFullException("Room has reached maximum capacity");
        }

        // Add member
        RoomMember member = RoomMember.builder()
                .id(new RoomMemberId(roomId, currentUserId))
                .room(room)
                .isOwner(false)
                .build();
        roomMemberRepository.save(member);

        // Broadcast new member to room members via websocket
        try {
            MemberResponse memberResponse = toMemberResponse(member);
            messagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/members", memberResponse);
        } catch (Exception e) {
            log.error("Failed to broadcast room join event: {}", e.getMessage(), e);
        }

        return toRoomResponse(room, currentUserId);
    }

    @Transactional
    public void leaveRoom(Long roomId, Long currentUserId) {
        log.info("User: {} leaving room: {}", currentUserId, roomId);

        Room room = getRoomOrThrow(roomId);

        // Owners cannot leave (must delete room or transfer ownership)
        if (roomRepository.isOwnerOfRoom(roomId, currentUserId)) {
            throw new IllegalStateException("Room owner cannot leave. Transfer ownership or delete room.");
        }

        RoomMemberId memberId = new RoomMemberId(roomId, currentUserId);
        roomMemberRepository.deleteById(memberId);

        // Broadcast member left event
        try {
            messagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/members/left", currentUserId);
        } catch (Exception e) {
            log.error("Failed to broadcast room leave event: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public void inviteMember(Long roomId, InviteMemberRequest request, Long currentUserId) {
        log.info("User: {} inviting user: {} to room: {}", currentUserId, request.getUserId(), roomId);

        validateMembership(roomId, currentUserId);
        Room room = getRoomOrThrow(roomId);

        // Check if user is already member
        if (roomRepository.existsMemberInRoom(roomId, request.getUserId())) {
            throw new IllegalStateException("User is already a member");
        }

        // Check capacity
        long memberCount = roomMemberRepository.countByRoomId(roomId);
        if (memberCount >= room.getMaxMembers()) {
            throw new RoomFullException("Room has reached maximum capacity");
        }

        RoomMember newMember = RoomMember.builder()
                .id(new RoomMemberId(roomId, request.getUserId()))
                .room(room)
                .isOwner(false)
                .build();
        roomMemberRepository.save(newMember);

        // Notify invited user via websocket (user-specific queue)
        try {
            MemberResponse resp = toMemberResponse(newMember);
            messagingTemplate.convertAndSendToUser(request.getUserId().toString(),
                    "/queue/rooms/invites", resp);
        } catch (Exception e) {
            log.error("Failed to send room invite notification: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public void removeMember(Long roomId, Long memberUserId, Long currentUserId) {
        log.info("User: {} removing user: {} from room: {}", currentUserId, memberUserId, roomId);

        validateOwnership(roomId, currentUserId);

        // Cannot remove owner
        if (roomRepository.isOwnerOfRoom(roomId, memberUserId)) {
            throw new IllegalStateException("Cannot remove room owner");
        }

        RoomMemberId memberId = new RoomMemberId(roomId, memberUserId);
        roomMemberRepository.deleteById(memberId);

        // Broadcast member removed event
        try {
            messagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/members/removed", memberUserId);
        } catch (Exception e) {
            log.error("Failed to broadcast member removal: {}", e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> getRoomMembers(Long roomId, Long currentUserId) {
        log.info("Getting members of room: {}", roomId);

        validateMembership(roomId, currentUserId);

        return roomMemberRepository.findByRoomId(roomId).stream()
                .map(this::toMemberResponse)
                .collect(Collectors.toList());
    }

    // Helper methods
    private Room getRoomOrThrow(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));
    }

    private void validateOwnership(Long roomId, Long userId) {
        if (!roomRepository.isOwnerOfRoom(roomId, userId)) {
            throw new UnauthorizedException("User is not the owner of this room");
        }
    }

    private void validateMembership(Long roomId, Long userId) {
        if (!roomRepository.existsMemberInRoom(roomId, userId)) {
            throw new UnauthorizedException("User is not a member of this room");
        }
    }

    private String generateInviteCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private RoomResponse toRoomResponse(Room room, Long currentUserId) {
        long memberCount = roomMemberRepository.countByRoomId(room.getId());
        boolean isOwner = roomRepository.isOwnerOfRoom(room.getId(), currentUserId);
        boolean isMember = roomRepository.existsMemberInRoom(room.getId(), currentUserId);

        // Map channels
        List<RoomResponse.ChannelInfo> channels = room.getChannels().stream()
                .map(channel -> RoomResponse.ChannelInfo.builder()
                .id(channel.getId())
                .name(channel.getName())
                .build())
                .collect(java.util.stream.Collectors.toList());

        return RoomResponse.builder()
                .id(room.getId())
                .name(room.getName())
                .description(room.getDescription())
                .creatorId(room.getCreatorId())
                .isPublic(room.getIsPublic())
                .inviteCode(room.getInviteCode())
                .memberCount((int) memberCount)
                .isOwner(isOwner)
                .isMember(isMember)
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .channels(channels)
                .build();
    }

    private RoomSummary toRoomSummary(Room room) {
        long memberCount = roomMemberRepository.countByRoomId(room.getId());

        return RoomSummary.builder()
                .id(room.getId())
                .name(room.getName())
                .memberCount((int) memberCount)
                .createdAt(room.getCreatedAt())
                .lastActivity(room.getUpdatedAt())
                .build();
    }

    private MemberResponse toMemberResponse(RoomMember member) {
        // TODO: Fetch user details from User Service via FeignClient
        return MemberResponse.builder()
                .userId(member.getId().getUserId())
                .username("user" + member.getId().getUserId()) // Placeholder
                .fullName("User " + member.getId().getUserId()) // Placeholder
                .avatarUrl(null)
                .isOwner(member.getIsOwner())
                .joinedAt(member.getJoinedAt())
                .build();
    }

    // ========== DIRECT MESSAGE METHODS ==========
    /**
     * Create or get existing Direct Message room between current user and
     * recipient
     *
     * - Check if DM room already exists between the two users - If exists,
     * return existing room - If not, create new DM room with roomType =
     * DIRECT_MESSAGE - DM rooms: isPublic=false, maxMembers=2, no invite code,
     * no channel concept
     */
    @Transactional
    public RoomResponse createOrGetDirectMessage(CreateDirectMessageRequest request, Long currentUserId) {
        Long recipientId = request.getRecipientUserId();

        log.info("Creating/Getting DM between user {} and user {}", currentUserId, recipientId);

        // Cannot DM yourself
        if (currentUserId.equals(recipientId)) {
            throw new IllegalArgumentException("Cannot create direct message with yourself");
        }

        // Check if DM already exists
        Optional<Room> existingDM = roomRepository.findDirectMessageRoom(
                RoomType.DIRECT_MESSAGE,
                currentUserId,
                recipientId
        );

        if (existingDM.isPresent()) {
            log.info("DM room already exists: {}", existingDM.get().getId());
            return toRoomResponse(existingDM.get(), currentUserId);
        }

        // Create new DM room
        // TODO: Fetch recipient user info from User Service to generate room name
        String dmName = "DM: " + currentUserId + " - " + recipientId;

        Room dmRoom = Room.builder()
                .name(dmName)
                .description("Direct message conversation")
                .creatorId(currentUserId)
                .isPublic(false)
                .roomType(RoomType.DIRECT_MESSAGE.toString())
                .maxMembers(2)
                .inviteCode(null) // No invite code for DMs
                .build();

        Room savedRoom = roomRepository.save(dmRoom);

        // Add both users as members (no owner concept in DMs)
        RoomMember member1 = RoomMember.builder()
                .id(new RoomMemberId(savedRoom.getId(), currentUserId))
                .room(savedRoom)
                .isOwner(false)
                .build();

        RoomMember member2 = RoomMember.builder()
                .id(new RoomMemberId(savedRoom.getId(), recipientId))
                .room(savedRoom)
                .isOwner(false)
                .build();

        roomMemberRepository.saveAll(List.of(member1, member2));

        log.info("Created new DM room: {}", savedRoom.getId());
        return toRoomResponse(savedRoom, currentUserId);
    }

    /**
     * Get all DM rooms for current user
     */
    @Transactional(readOnly = true)
    public List<RoomSummary> getUserDirectMessages(Long userId) {
        log.info("Getting DMs for user: {}", userId);

        return roomRepository.findRoomsByUserId(userId).stream()
                .filter(room -> room.getRoomType() == RoomType.DIRECT_MESSAGE.toString())
                .map(this::toRoomSummary)
                .collect(Collectors.toList());
    }
}
