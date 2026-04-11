package com.studyhub.ai_service.client;

import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "media-service")
public interface MediaServiceClient {

    @GetMapping("/api/v1/media/files/{fileId}/download")
    Response downloadFile(@PathVariable("fileId") String fileId);
}
