package com.studyhub.chat_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponse {

    private Long id;
    private String name;
    private String description;
    private String creatorId;
    private Boolean isPublic;
    private String inviteCode;
    private Integer memberCount;
    private Boolean isOwner;
    private Boolean isMember;
    private Instant createdAt;
    private Instant updatedAt;
    private List<ChannelInfo> channels;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChannelInfo {

        private Long id;
        private String name;
    }
}
