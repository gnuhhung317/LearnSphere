package com.studyhub.learningservice.event;

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
    private String type; // FILE, LINK, TEXT
    private String url; // For FILE this is ID/Path
    private String userId;
}
