package com.studyhub.search_service.client;

import com.studyhub.search_service.dto.RoomSummaryDto;
import com.studyhub.search_service.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "chat-service", configuration = AuthRequestInterceptor.class)
public interface ChatClient {
    @GetMapping("/api/v1/rooms/search")
    ApiResponse<List<RoomSummaryDto>> searchRooms(@RequestParam("q") String query);
}
