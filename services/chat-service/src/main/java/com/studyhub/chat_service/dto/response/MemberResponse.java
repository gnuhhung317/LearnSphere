package com.studyhub.chat_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberResponse {

    private String userId;
    private String username;
    private String fullName;
    private String avatarUrl;
    private Boolean isOwner;
    private Instant joinedAt;
}
