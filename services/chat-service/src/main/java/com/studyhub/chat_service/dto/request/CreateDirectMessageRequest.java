package com.studyhub.chat_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to initiate a direct message conversation with another user
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDirectMessageRequest {

    @NotNull(message = "Recipient user ID is required")
    private Long recipientUserId;
}
