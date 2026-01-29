package com.studyhub.learningservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceDeletedEvent {
    private Long resourceId;
    private Long learningSpaceId;
    private String type;
    private String url;
}
