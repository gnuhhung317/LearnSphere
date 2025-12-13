package com.studyhub.chat_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendMessageRequest {

    // Content is optional if attachments are present
    @Size(max = 5000, message = "Message content must not exceed 5000 characters")
    private String content;

    private Long parentMessageId;

    private List<AttachmentDto> attachments;
}
