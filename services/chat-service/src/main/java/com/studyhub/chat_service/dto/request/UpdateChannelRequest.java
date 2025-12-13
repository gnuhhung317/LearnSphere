package com.studyhub.chat_service.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateChannelRequest {
    
    @Size(min = 1, max = 50, message = "Channel name must be between 1 and 50 characters")
    private String name;
}
