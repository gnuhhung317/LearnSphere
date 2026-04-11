package com.studyhub.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePreferencesRequest {
    private Map<String, Boolean> notifications;
    private Map<String, Boolean> accessibility;
    private Map<String, Object> privacy;
    private Map<String, Object> aiSettings;

    // allow updating single preference section or all at once
}
