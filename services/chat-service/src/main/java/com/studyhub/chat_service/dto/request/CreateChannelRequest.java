package com.studyhub.chat_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateChannelRequest {
    
    @NotNull(message = "Room ID is required")
    private Long roomId;
    
    @NotBlank(message = "Channel name is required")
    @Size(min = 1, max = 50, message = "Channel name must be between 1 and 50 characters")
    private String name;
}
