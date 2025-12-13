package com.studyhub.chat_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddReactionRequest {
    
    @NotBlank(message = "Emoji is required")
    private String emoji;
}
