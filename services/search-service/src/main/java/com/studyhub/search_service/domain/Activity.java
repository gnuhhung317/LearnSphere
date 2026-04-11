package com.studyhub.search_service.domain;

import com.studyhub.search_service.dto.ActivityItemDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String userId;

    @Enumerated(EnumType.STRING)
    private ActivityItemDto.ActivityType type;

    private String title;
    private String description;
    private LocalDateTime timestamp;
    private String link;
    private String metadata;
}
