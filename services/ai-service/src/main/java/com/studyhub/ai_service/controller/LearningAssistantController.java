package com.studyhub.ai_service.controller;

import com.studyhub.ai_service.dto.*;
import com.studyhub.ai_service.service.LearningAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai/learning")
@RequiredArgsConstructor
public class LearningAssistantController {

    private final LearningAssistantService learningAssistantService;

    @PostMapping("/quiz")
    public ResponseEntity<GeneratedQuizResponse> generateQuiz(@RequestBody GenerateQuizRequest request) {
        return ResponseEntity.ok(learningAssistantService.generateQuiz(request));
    }

    @PostMapping("/flashcards")
    public ResponseEntity<GeneratedFlashcardsResponse> generateFlashcards(
            @RequestBody GenerateFlashcardsRequest request) {
        return ResponseEntity.ok(learningAssistantService.generateFlashcards(request));
    }

    @PostMapping("/course-plan")
    public ResponseEntity<GeneratedCoursePlan> generateCoursePlan(@RequestBody GeneratedCoursePlanRequest request) {
        return ResponseEntity.ok(learningAssistantService.generateCoursePlan(request));
    }
}
