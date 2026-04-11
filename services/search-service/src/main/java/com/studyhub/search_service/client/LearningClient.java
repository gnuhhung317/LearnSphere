package com.studyhub.search_service.client;

import com.studyhub.search_service.dto.LearningSpaceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "learning-service", configuration = AuthRequestInterceptor.class)
public interface LearningClient {
    @GetMapping("/api/v1/learning-spaces/search")
    List<LearningSpaceDto> searchLearningSpaces(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam("q") String query);

    @GetMapping("/api/v1/learning-spaces/recent")
    List<LearningSpaceDto> getRecentLearningSpaces(@RequestHeader("X-User-Id") Long userId);
}
