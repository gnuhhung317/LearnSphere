package com.studyhub.user_service.dto;

import lombok.Data;

@Data
public class UserInfo {
    private String id;
    private String username;
    private String fullName;
    private String avatarUrl;
}
