package com.studyhub.search_service.event.listener;

import com.studyhub.search_service.domain.Activity;
import com.studyhub.search_service.dto.ActivityItemDto;
import com.studyhub.search_service.event.LearningSpaceCreatedEvent;
import com.studyhub.search_service.event.ResourceAddedEvent;
import com.studyhub.search_service.event.RoomCreatedEvent;
import com.studyhub.search_service.event.UserJoinedRoomEvent;
import com.studyhub.search_service.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@Slf4j
@RequiredArgsConstructor
public class ActivityEventListener {

        private final ActivityRepository activityRepository;

        @KafkaListener(topics = "chat.rooms.created", properties = {
                        "spring.json.value.default.type=com.studyhub.search_service.event.RoomCreatedEvent"
        })
        public void handleRoomCreated(RoomCreatedEvent event) {
                log.info("Handling RoomCreatedEvent for activity logging: {}", event.getRoomId());
                Activity activity = Activity.builder()
                                .userId(event.getCreatorId())
                                .type(ActivityItemDto.ActivityType.ROOM_JOINED) // Using ROOM_JOINED as a proxy for
                                                                                // participation
                                .title("Created group: " + event.getRoomName())
                                .description(event.getDescription())
                                .timestamp(LocalDateTime.now())
                                .link("/workspace/group/" + event.getRoomId())
                                .build();
                activityRepository.save(activity);
        }

        @KafkaListener(topics = "chat.rooms.user-joined", properties = {
                        "spring.json.value.default.type=com.studyhub.search_service.event.UserJoinedRoomEvent"
        })
        public void handleUserJoinedRoom(UserJoinedRoomEvent event) {
                log.info("Handling UserJoinedRoomEvent for activity logging: userId={}, roomId={}", event.getUserId(),
                                event.getRoomId());
                Activity activity = Activity.builder()
                                .userId(event.getUserId())
                                .type(ActivityItemDto.ActivityType.ROOM_JOINED)
                                .title("Joined group: " + (event.getRoomName() != null ? event.getRoomName()
                                                : event.getRoomId()))
                                .timestamp(LocalDateTime.now())
                                .link("/workspace/group/" + event.getRoomId())
                                .build();
                activityRepository.save(activity);
        }

        @KafkaListener(topics = "learning.space.created", properties = {
                        "spring.json.value.default.type=com.studyhub.search_service.event.LearningSpaceCreatedEvent"
        })
        public void handleLearningSpaceCreated(LearningSpaceCreatedEvent event) {
                log.info("Handling LearningSpaceCreatedEvent for activity logging: {}", event.getId());
                Activity activity = Activity.builder()
                                .userId(event.getUserId())
                                .type(ActivityItemDto.ActivityType.COURSE)
                                .title("Created learning space: " + event.getTitle())
                                .description(event.getDescription())
                                .timestamp(event.getCreatedAt() != null ? event.getCreatedAt() : LocalDateTime.now())
                                .link("/learning/" + event.getId())
                                .build();
                activityRepository.save(activity);
        }

        @KafkaListener(topics = "learning.resource.added", properties = {
                        "spring.json.value.default.type=com.studyhub.search_service.event.ResourceAddedEvent"
        })
        public void handleResourceAdded(ResourceAddedEvent event) {
                log.info("Handling ResourceAddedEvent for activity logging: {}", event.getResourceId());
                Activity activity = Activity.builder()
                                .userId(event.getUserId())
                                .type(ActivityItemDto.ActivityType.FILE)
                                .title("Added resource: " + event.getTitle())
                                .description(event.getType())
                                .timestamp(LocalDateTime.now())
                                .link("/learning/" + event.getLearningSpaceId())
                                .build();
                activityRepository.save(activity);
        }
}
