package com.studyhub.chat_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "ai-service", url = "${application.config.ai-service-url:http://ai-service:8086}")
public interface AiClient {

    @PostMapping("/api/v1/ai/chat")
    Map<String, String> chat(@RequestBody Map<String, Object> request);
}
