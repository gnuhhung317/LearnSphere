package com.studyhub.chat_service.service;

import com.studyhub.chat_service.dto.request.CreateChannelRequest;
import com.studyhub.chat_service.dto.request.UpdateChannelRequest;
import com.studyhub.chat_service.dto.response.ChannelResponse;
import com.studyhub.chat_service.entity.Channel;
import com.studyhub.chat_service.entity.Room;
import com.studyhub.chat_service.exception.ChannelNotFoundException;
import com.studyhub.chat_service.exception.UnauthorizedException;
import com.studyhub.chat_service.repository.ChannelRepository;
import com.studyhub.chat_service.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelService {

    private static final String DEFAULT_CHANNEL_NAME = "General";
    private static final int MAX_CHANNELS_PER_ROOM = 50;

    private final ChannelRepository channelRepository;
    private final RoomRepository roomRepository;

    @Transactional
    public ChannelResponse createChannel(CreateChannelRequest request, String currentUserId) {
        log.info("Creating channel: {} in room: {} by user: {}", 
            request.getName(), request.getRoomId(), currentUserId);

        Room room = getRoomOrThrow(request.getRoomId());
        
        // Only room owner can create channels
        if (!roomRepository.isOwnerOfRoom(request.getRoomId(), currentUserId)) {
            throw new UnauthorizedException("Only room owner can create channels");
        }

        // Check channel limit
        long channelCount = channelRepository.countByRoomId(request.getRoomId());
        if (channelCount >= MAX_CHANNELS_PER_ROOM) {
            throw new IllegalStateException("Room has reached maximum channel limit");
        }

        // Check for duplicate channel name in the room
        if (channelRepository.existsByRoomIdAndName(request.getRoomId(), request.getName())) {
            throw new IllegalArgumentException("Channel name already exists in this room");
        }

        Channel channel = Channel.builder()
                .name(request.getName())
                .room(room)
                .build();

        Channel savedChannel = channelRepository.save(channel);
        return toChannelResponse(savedChannel);
    }

    @Transactional
    public ChannelResponse updateChannel(Long channelId, UpdateChannelRequest request, String currentUserId) {
        log.info("Updating channel: {} by user: {}", channelId, currentUserId);

        Channel channel = getChannelOrThrow(channelId);
        
        // Only room owner can update channels
        if (!roomRepository.isOwnerOfRoom(channel.getRoom().getId(), currentUserId)) {
            throw new UnauthorizedException("Only room owner can update channels");
        }

        // Cannot rename default General channel
        if (DEFAULT_CHANNEL_NAME.equalsIgnoreCase(channel.getName())) {
            throw new IllegalStateException("Cannot rename the default General channel");
        }

        // Check for duplicate name
        if (request.getName() != null && 
            !request.getName().equals(channel.getName()) &&
            channelRepository.existsByRoomIdAndName(channel.getRoom().getId(), request.getName())) {
            throw new IllegalArgumentException("Channel name already exists in this room");
        }

        if (request.getName() != null) {
            channel.setName(request.getName());
        }

        Channel updatedChannel = channelRepository.save(channel);
        return toChannelResponse(updatedChannel);
    }

    @Transactional
    public void deleteChannel(Long channelId, String currentUserId) {
        log.info("Deleting channel: {} by user: {}", channelId, currentUserId);

        Channel channel = getChannelOrThrow(channelId);
        
        // Only room owner can delete channels
        if (!roomRepository.isOwnerOfRoom(channel.getRoom().getId(), currentUserId)) {
            throw new UnauthorizedException("Only room owner can delete channels");
        }

        // Cannot delete default General channel
        if (DEFAULT_CHANNEL_NAME.equalsIgnoreCase(channel.getName())) {
            throw new IllegalStateException("Cannot delete the default General channel");
        }

        // Check if it's the last channel
        long channelCount = channelRepository.countByRoomId(channel.getRoom().getId());
        if (channelCount <= 1) {
            throw new IllegalStateException("Cannot delete the last channel in a room");
        }

        channelRepository.delete(channel);
    }

    @Transactional(readOnly = true)
    public ChannelResponse getChannelById(Long channelId, String currentUserId) {
        Channel channel = getChannelOrThrow(channelId);
        
        // Validate membership
        if (!roomRepository.existsMemberInRoom(channel.getRoom().getId(), currentUserId)) {
            throw new UnauthorizedException("User is not a member of this room");
        }

        return toChannelResponse(channel);
    }

    @Transactional(readOnly = true)
    public List<ChannelResponse> getRoomChannels(Long roomId, String currentUserId) {
        log.info("Getting channels for room: {}", roomId);

        // Validate membership
        if (!roomRepository.existsMemberInRoom(roomId, currentUserId)) {
            throw new UnauthorizedException("User is not a member of this room");
        }

        List<Channel> channels = channelRepository.findByRoomIdOrderByIdAsc(roomId);
        return channels.stream()
                .map(this::toChannelResponse)
                .collect(Collectors.toList());
    }

    // Helper methods
    private Channel getChannelOrThrow(Long channelId) {
        return channelRepository.findById(channelId)
                .orElseThrow(() -> new ChannelNotFoundException("Channel not found with id: " + channelId));
    }

    private Room getRoomOrThrow(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with id: " + roomId));
    }

    private ChannelResponse toChannelResponse(Channel channel) {
        return ChannelResponse.builder()
                .id(channel.getId())
                .roomId(channel.getRoom().getId())
                .name(channel.getName())
                .build();
    }
}
