package com.studyhub.ai_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int totalPages;
    private long totalElements;
    private int size;
    private int number;
}
