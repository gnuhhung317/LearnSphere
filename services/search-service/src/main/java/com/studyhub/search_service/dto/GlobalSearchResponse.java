package com.studyhub.search_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalSearchResponse {
    private List<UserSummaryDto> users;
    private List<LearningSpaceDto> learningSpaces;
    private List<RoomSummaryDto> rooms;
    private List<MediaFileDto> files;
}
