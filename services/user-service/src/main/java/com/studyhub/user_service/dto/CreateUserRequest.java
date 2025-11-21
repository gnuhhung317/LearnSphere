package com.studyhub.user_service.dto;

import com.studyhub.common.constant.enums.SupportedLanguage;
import com.studyhub.common.constant.enums.Theme;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "Keycloak user id is required")
    private String keycloakUserId;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Full name is required")
    @Size(max = 50, message = "Full name must not exceed 50 characters")
    private String fullName;

    private Theme theme = Theme.AUTO;

    private SupportedLanguage language = SupportedLanguage.ENGLISH;


}
