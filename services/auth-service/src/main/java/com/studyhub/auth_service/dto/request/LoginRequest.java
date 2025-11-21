package com.studyhub.auth_service.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @Email(message = "Email is not valid")
    private String email;
    private String password;
}
