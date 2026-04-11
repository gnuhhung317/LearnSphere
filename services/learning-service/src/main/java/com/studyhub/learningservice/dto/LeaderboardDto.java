package com.studyhub.learningservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardDto {
    private List<LeaderboardEntryDto> topUsers;
    private LeaderboardEntryDto currentUser;
    private String period; // "WEEKLY", "ALL_TIME"
}
