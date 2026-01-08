package com.studyhub.ai_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "chat-service")
public interface ChatServiceClient {

    @PutMapping("/api/v1/files/{attachmentId}/status")
    void updateFileStatus(@PathVariable("attachmentId") Long attachmentId, @RequestParam("status") String status);
}
