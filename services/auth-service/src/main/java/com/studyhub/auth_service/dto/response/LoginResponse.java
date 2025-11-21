package com.studyhub.auth_service.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String idToken;
    private Long expiresIn;
    private Long refreshExpiresIn;
    private int notBeforePolicy;
    private String scope;
    private String sessionState;
}
