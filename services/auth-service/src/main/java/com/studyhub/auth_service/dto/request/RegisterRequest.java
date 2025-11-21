package com.studyhub.auth_service.dto.request;

import com.studyhub.common.constant.enums.SupportedLanguage;
import com.studyhub.common.constant.enums.Theme;
import lombok.Data;

@Data
public class RegisterRequest {
    private String fullName;
    private String email;
    private String password;
    private Theme theme;
    private SupportedLanguage voiceRecognitionLanguage;
    private SupportedLanguage aiResponseLanguage;
}
