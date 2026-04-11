package com.studyhub.learningservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementDto {
    private String id;
    private String name;
    private String description;
    private String icon;
    private String unlockedAt; // ISO Date or null
    private int progress;
    private int maxProgress;
}
