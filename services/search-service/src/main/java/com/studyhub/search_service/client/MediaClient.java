package com.studyhub.search_service.client;

import com.studyhub.search_service.dto.MediaFileDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "media-service", configuration = AuthRequestInterceptor.class)
public interface MediaClient {
    @GetMapping("/api/v1/media/search")
    List<MediaFileDto> searchFiles(@RequestParam("q") String query);

    @GetMapping("/api/v1/media/recent")
    List<MediaFileDto> getRecentFiles();
}
