package com.studyhub.search_service.service;

import com.studyhub.search_service.client.ChatClient;
import com.studyhub.search_service.client.LearningClient;
import com.studyhub.search_service.client.MediaClient;
import com.studyhub.search_service.client.UserClient;
import com.studyhub.search_service.dto.*;
import com.studyhub.search_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final UserClient userClient;
    private final LearningClient learningClient;
    private final ChatClient chatClient;
    private final MediaClient mediaClient;
    private final com.studyhub.search_service.repository.ActivityRepository activityRepository;

    public GlobalSearchResponse globalSearch(String query) {
        String userId = JwtUtil.getUserIdFromJwt();
        log.info("Performing global search for query: '{}', userId: {}", query, userId);
        Long userIdLong = userId != null ? tryParseLong(userId) : null;

        CompletableFuture<List<UserSummaryDto>> usersFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return userClient.searchUsers(query);
            } catch (Exception e) {
                log.error("Error searching users", e);
                return Collections.emptyList();
            }
        });

        CompletableFuture<List<LearningSpaceDto>> learningFuture = CompletableFuture.supplyAsync(() -> {
            try {
                // Learning service expects Long userId
                if (userIdLong != null) {
                    return learningClient.searchLearningSpaces(userIdLong, query);
                }
                return Collections.emptyList();
            } catch (Exception e) {
                log.error("Error searching learning spaces", e);
                return Collections.emptyList();
            }
        });

        CompletableFuture<List<RoomSummaryDto>> roomsFuture = CompletableFuture.supplyAsync(() -> {
            try {
                ApiResponse<List<RoomSummaryDto>> response = chatClient.searchRooms(query);
                if (response != null && response.getData() != null) {
                    return response.getData();
                }
                return Collections.emptyList();
            } catch (Exception e) {
                log.error("Error searching rooms", e);
                return Collections.emptyList();
            }
        });

        CompletableFuture<List<MediaFileDto>> filesFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return mediaClient.searchFiles(query);
            } catch (Exception e) {
                log.error("Error searching media files", e);
                return Collections.emptyList();
            }
        });

        try {
            CompletableFuture.allOf(usersFuture, learningFuture, roomsFuture, filesFuture).join();

            return GlobalSearchResponse.builder()
                    .users(usersFuture.get())
                    .learningSpaces(learningFuture.get())
                    .rooms(roomsFuture.get())
                    .files(filesFuture.get())
                    .build();

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error aggregating search results", e);
            return GlobalSearchResponse.builder()
                    .users(Collections.emptyList())
                    .learningSpaces(Collections.emptyList())
                    .rooms(Collections.emptyList())
                    .files(Collections.emptyList())
                    .build();
        }
    }

    private Long tryParseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public List<ActivityItemDto> getRecentActivity() {
        String userId = JwtUtil.getUserIdFromJwt();
        log.info("Fetching recent activity for userId: {}", userId);

        List<ActivityItemDto> activities = new java.util.ArrayList<>();

        // 1. Fetch persistent activities from database
        try {
            activityRepository.findTop10ByUserIdOrderByTimestampDesc(userId).forEach(activity -> {
                activities.add(ActivityItemDto.builder()
                        .id(activity.getId())
                        .type(activity.getType())
                        .title(activity.getTitle())
                        .description(activity.getDescription())
                        .timestamp(activity.getTimestamp())
                        .link(activity.getLink())
                        .metadata(activity.getMetadata())
                        .build());
            });
        } catch (Exception e) {
            log.error("Error fetching persistent activities", e);
        }

        // 2. Aggregate on-the-fly recent items if list is still small
        if (activities.size() < 5) {
            Long userIdLong = userId != null ? tryParseLong(userId) : null;

            CompletableFuture<List<LearningSpaceDto>> learningFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    if (userIdLong != null) {
                        return learningClient.getRecentLearningSpaces(userIdLong);
                    }
                    return Collections.emptyList();
                } catch (Exception e) {
                    log.error("Error fetching recent learning spaces", e);
                    return Collections.emptyList();
                }
            });

            CompletableFuture<List<MediaFileDto>> filesFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return mediaClient.getRecentFiles();
                } catch (Exception e) {
                    log.error("Error fetching recent files", e);
                    return Collections.emptyList();
                }
            });

            try {
                CompletableFuture.allOf(learningFuture, filesFuture).join();

                // Map Learning Spaces (avoid duplicates if already in persistent list)
                learningFuture.get().forEach(space -> {
                    String activityId = "ls-" + space.getId();
                    if (activities.stream().noneMatch(a -> a.getLink().contains("/learning/" + space.getId()))) {
                        activities.add(ActivityItemDto.builder()
                                .id(activityId)
                                .type(ActivityItemDto.ActivityType.COURSE)
                                .title(space.getTitle())
                                .description(space.getDescription())
                                .timestamp(space.getUpdatedAt() != null ? space.getUpdatedAt() : space.getCreatedAt())
                                .link("/learning/" + space.getId())
                                .build());
                    }
                });

                // Map Files
                filesFuture.get().forEach(file -> {
                    if (activities.stream().noneMatch(a -> a.getTitle().contains(file.getOriginalFilename()))) {
                        activities.add(ActivityItemDto.builder()
                                .id("file-" + file.getId())
                                .type(ActivityItemDto.ActivityType.FILE)
                                .title(file.getOriginalFilename())
                                .description(file.getFileType())
                                .timestamp(file.getUploadedAt())
                                .link("/hub")
                                .build());
                    }
                });

            } catch (InterruptedException | ExecutionException e) {
                log.error("Error aggregating recent activity", e);
            }
        }

        // Sort by Timestamp Descending
        activities.sort((a, b) -> {
            if (a.getTimestamp() == null)
                return 1;
            if (b.getTimestamp() == null)
                return -1;
            return b.getTimestamp().compareTo(a.getTimestamp());
        });

        // Limit to top 20
        return activities.stream().limit(20).collect(Collectors.toList());
    }
}
