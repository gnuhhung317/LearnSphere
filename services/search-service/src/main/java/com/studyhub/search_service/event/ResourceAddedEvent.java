package com.studyhub.search_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceAddedEvent {
    private Long resourceId;
    private Long learningSpaceId;
    private String title;
    private String type;
    private String url;
    private String userId;
}
