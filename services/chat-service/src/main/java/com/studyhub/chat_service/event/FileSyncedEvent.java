package com.studyhub.chat_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileSyncedEvent {
    private String eventId;
    private Long timestamp;
    private Long attachmentId;
    private String fileId;
    private Long messageId;
    private Long roomId;
    private String aiStatus;
}
