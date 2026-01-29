package com.studyhub.search_service.controller;

import com.studyhub.search_service.dto.GlobalSearchResponse;
import com.studyhub.search_service.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<GlobalSearchResponse> search(@RequestParam("q") String query) {
        log.info("Received search request for query: {}", query);
        GlobalSearchResponse response = searchService.globalSearch(query);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/activity/recent")
    public ResponseEntity<java.util.List<com.studyhub.search_service.dto.ActivityItemDto>> getRecentActivity() {
        return ResponseEntity.ok(searchService.getRecentActivity());
    }
}
