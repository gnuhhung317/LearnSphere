package com.studyhub.ai_service.client;

import com.studyhub.ai_service.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.Builder;
import lombok.Data;

@FeignClient(name = "learning-service", configuration = FeignClientConfig.class)
public interface LearningServiceClient {

    @PatchMapping("/api/v1/learning-spaces/{learningSpaceId}/resources/{resourceId}")
    void updateResource(
            @PathVariable("learningSpaceId") Long learningSpaceId,
            @PathVariable("resourceId") Long resourceId,
            @RequestBody UpdateResourceRequest request);

    @org.springframework.web.bind.annotation.PostMapping("/api/v1/learning-spaces/{learningSpaceId}/resources/{resourceId}/flashcards")
    void createFlashcardDeck(
            @PathVariable("learningSpaceId") Long learningSpaceId,
            @PathVariable("resourceId") Long resourceId,
            @RequestBody FlashcardDeckDto request);

    @Data
    @Builder
    class UpdateResourceRequest {
        private String title;
        private String description;
        private Boolean isCompleted;
        private String status;
    }

    @Data
    @Builder
    class FlashcardDeckDto {
        private String title;
        private java.util.List<FlashcardDto> flashcards;
    }

    @Data
    @Builder
    class FlashcardDto {
        private String front;
        private String back;
    }
}
