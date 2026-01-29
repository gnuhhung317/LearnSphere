package com.studyhub.ai_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.studyhub.ai_service.config.FeignClientConfig;

@FeignClient(name = "chat-service", configuration = FeignClientConfig.class)
public interface ChatServiceClient {

    @PutMapping("/api/v1/files/{attachmentId}/status")
    void updateFileStatus(@PathVariable("attachmentId") Long attachmentId, @RequestParam("status") String status);

    @org.springframework.web.bind.annotation.GetMapping("/api/v1/messages/channels/{channelId}")
    com.studyhub.ai_service.dto.ApiResponse<com.studyhub.ai_service.dto.PageResponse<com.studyhub.ai_service.dto.MessageDto>> getMessageHistory(
            @PathVariable("channelId") Long channelId,
            @RequestParam("page") int page,
            @RequestParam("size") int size);
}
