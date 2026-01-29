package com.studyhub.learningservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizDto {
    private Long id;
    private String title;
    private String description;
    private Long resourceId;
    private List<QuestionDto> questions;
}
