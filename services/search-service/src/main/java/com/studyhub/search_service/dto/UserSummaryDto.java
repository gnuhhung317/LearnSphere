package com.studyhub.search_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDto {
    private String id; // keycloak ID
    private Long numericId; // database ID
    private String username;
    private String fullName;
    private String avatarUrl;
    private String bio;
}
