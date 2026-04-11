package com.studyhub.learningservice.dto;

import com.studyhub.learningservice.domain.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDto {
    private Long id;
    private String text;
    private QuestionType type;
    private List<String> options;
    private String correctAnswer; // Note: In a real app, might want to hide this until submission
    private String explanation;
}
