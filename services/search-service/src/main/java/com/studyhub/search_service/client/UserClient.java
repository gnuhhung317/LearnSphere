package com.studyhub.search_service.client;

import com.studyhub.search_service.dto.UserSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "user-service", configuration = AuthRequestInterceptor.class)
public interface UserClient {
    @GetMapping("/api/v1/users/search")
    List<UserSummaryDto> searchUsers(@RequestParam("q") String query);
}
