package com.studyhub.chat_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferOwnershipRequest {
    
    @NotNull(message = "New owner ID is required")
    private String newOwnerId;
}
