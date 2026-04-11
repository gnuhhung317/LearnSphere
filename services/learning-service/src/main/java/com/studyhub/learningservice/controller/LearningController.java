package com.studyhub.learningservice.controller;

import com.studyhub.learningservice.dto.AddResourceRequest;
import com.studyhub.learningservice.dto.CreateLearningSpaceRequest;
import com.studyhub.learningservice.dto.LearningSpaceDto;
import com.studyhub.learningservice.dto.QuizDto;
import com.studyhub.learningservice.dto.FlashcardDeckDto;
import com.studyhub.learningservice.dto.ResourceDto;
import com.studyhub.learningservice.dto.UpdateLearningSpaceRequest;
import com.studyhub.learningservice.service.LearningService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/learning-spaces")
@RequiredArgsConstructor
public class LearningController {

    private final LearningService learningService;

    @PostMapping
    public ResponseEntity<LearningSpaceDto> createLearningSpace(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateLearningSpaceRequest request) {
        return ResponseEntity.ok(learningService.createLearningSpace(userId, request));
    }

    @PostMapping("/structure")
    public ResponseEntity<LearningSpaceDto> createCourseFromStructure(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody com.studyhub.learningservice.dto.CreateCourseStructureRequest request) {
        return ResponseEntity.ok(learningService.createCourseFromStructure(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<LearningSpaceDto>> getUserLearningSpaces(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(learningService.getUserLearningSpaces(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<LearningSpaceDto>> searchLearningSpaces(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("q") String query) {
        return ResponseEntity.ok(learningService.searchLearningSpaces(query, userId));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<LearningSpaceDto>> getRecentLearningSpaces(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(learningService.getRecentLearningSpaces(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LearningSpaceDto> getLearningSpace(@PathVariable Long id) {
        return ResponseEntity.ok(learningService.getLearningSpace(id));
    }

    @PostMapping("/{id}/resources")
    public ResponseEntity<ResourceDto> addResource(
            @PathVariable Long id,
            @Valid @RequestBody AddResourceRequest request) {
        return ResponseEntity.ok(learningService.addResource(id, request));
    }

    @PatchMapping("/{id}/resources/{resourceId}")
    public ResponseEntity<ResourceDto> updateResource(
            @PathVariable Long id,
            @PathVariable Long resourceId,
            @RequestBody com.studyhub.learningservice.dto.UpdateResourceRequest request) {
        return ResponseEntity.ok(learningService.updateResource(id, resourceId, request));
    }

    @DeleteMapping("/{id}/resources/{resourceId}")
    public ResponseEntity<Void> deleteResource(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long id,
            @PathVariable Long resourceId) {
        learningService.deleteResource(id, resourceId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLearningSpace(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long id) {
        learningService.deleteLearningSpace(id, userId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<LearningSpaceDto> updateLearningSpace(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long id,
            @RequestBody UpdateLearningSpaceRequest request) {
        return ResponseEntity.ok(learningService.updateLearningSpace(id, userId, request));
    }

    // Quiz & Flashcard Endpoints

    @PostMapping("/{learningSpaceId}/resources/{resourceId}/quiz")
    public ResponseEntity<QuizDto> createQuiz(
            @PathVariable Long learningSpaceId,
            @PathVariable Long resourceId,
            @RequestBody QuizDto quizDto) {
        return ResponseEntity.ok(learningService.createQuiz(resourceId, quizDto));
    }

    @GetMapping("/{learningSpaceId}/resources/{resourceId}/quiz")
    public ResponseEntity<List<QuizDto>> getQuizzes(@PathVariable Long learningSpaceId, @PathVariable Long resourceId) {
        return ResponseEntity.ok(learningService.getQuizzesByResource(resourceId));
    }

    @PostMapping("/{learningSpaceId}/resources/{resourceId}/flashcards")
    public ResponseEntity<FlashcardDeckDto> createFlashcardDeck(
            @PathVariable Long learningSpaceId,
            @PathVariable Long resourceId,
            @RequestBody FlashcardDeckDto deckDto) {
        return ResponseEntity.ok(learningService.createFlashcardDeck(resourceId, deckDto));
    }

    @GetMapping("/{learningSpaceId}/resources/{resourceId}/flashcards")
    public ResponseEntity<List<FlashcardDeckDto>> getFlashcardDecks(@PathVariable Long learningSpaceId,
            @PathVariable Long resourceId) {
        return ResponseEntity.ok(learningService.getFlashcardDecksByResource(resourceId));
    }

    @GetMapping("/{learningSpaceId}/resources/{resourceId}/flashcards/{deckId}/due")
    public ResponseEntity<List<com.studyhub.learningservice.dto.FlashcardDto>> getDueFlashcards(
            @PathVariable Long learningSpaceId,
            @PathVariable Long resourceId,
            @PathVariable Long deckId) {
        return ResponseEntity.ok(learningService.getDueFlashcards(deckId));
    }

    @PostMapping("/{learningSpaceId}/resources/{resourceId}/flashcards/{flashcardId}/review")
    public ResponseEntity<com.studyhub.learningservice.dto.FlashcardDto> reviewFlashcard(
            @PathVariable Long learningSpaceId,
            @PathVariable Long resourceId,
            @PathVariable Long flashcardId,
            @RequestBody ReviewFlashcardRequest request) {
        return ResponseEntity.ok(learningService.reviewFlashcard(flashcardId, request.getRating()));
    }

    @GetMapping("/flashcards/due/count")
    public ResponseEntity<Long> countDueFlashcards(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(learningService.countDueFlashcards(userId));
    }

    // ==================== Study Mode Endpoints ====================

    /**
     * Get all due flashcards for the current user across all learning spaces.
     * Used by the Study Dashboard.
     */
    @GetMapping("/study/due")
    public ResponseEntity<java.util.List<com.studyhub.learningservice.dto.StudyFlashcardDto>> getAllDueFlashcards(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(learningService.getAllDueFlashcards(userId));
    }

    /**
     * Get study statistics including streak, cards reviewed, etc.
     */
    @GetMapping("/study/stats")
    public ResponseEntity<com.studyhub.learningservice.dto.StudyStatsDto> getStudyStats(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(learningService.getStudyStats(userId));
    }

    /**
     * Review a flashcard in study mode. Updates both the flashcard SRS data
     * and the user's study stats (streak, daily count, etc.)
     */
    @PostMapping("/study/review/{flashcardId}")
    public ResponseEntity<com.studyhub.learningservice.dto.FlashcardDto> reviewFlashcardInStudyMode(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long flashcardId,
            @RequestBody ReviewFlashcardRequest request) {
        return ResponseEntity.ok(learningService.reviewFlashcardWithStats(userId, flashcardId, request.getRating()));
    }

    // ==================== Progress Dashboard Endpoints ====================

    /**
     * Get comprehensive progress dashboard data including heatmap, stats, and
     * trends.
     */
    @GetMapping("/progress")
    public ResponseEntity<com.studyhub.learningservice.dto.ProgressDashboardDto> getProgressDashboard(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(learningService.getProgressDashboard(userId));
    }

    // ==================== Daily Goals Endpoints ====================

    @GetMapping("/goals")
    public ResponseEntity<com.studyhub.learningservice.dto.DailyGoalDto> getDailyGoals(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(learningService.getDailyGoals(userId));
    }

    @PutMapping("/goals")
    public ResponseEntity<com.studyhub.learningservice.dto.DailyGoalDto> updateDailyGoals(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody com.studyhub.learningservice.dto.DailyGoalDto goalsDto) {
        return ResponseEntity.ok(learningService.updateDailyGoals(userId, goalsDto));
    }

    @GetMapping("/goals/progress")
    public ResponseEntity<com.studyhub.learningservice.dto.DailyGoalProgressDto> getDailyGoalProgress(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(learningService.getDailyGoalProgress(userId));
    }

    // ==================== Leaderboard Endpoints ====================

    @GetMapping("/leaderboard")
    public ResponseEntity<com.studyhub.learningservice.dto.LeaderboardDto> getLeaderboard(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(learningService.getLeaderboard(userId));
    }

    // ==================== Profile Endpoints ====================

    @GetMapping("/achievements")
    public ResponseEntity<java.util.List<com.studyhub.learningservice.dto.AchievementDto>> getAchievements(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(learningService.getAchievements(userId));
    }

    @GetMapping("/learning-paths")
    public ResponseEntity<java.util.List<com.studyhub.learningservice.dto.LearningSpaceDto>> getLearningPaths(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(learningService.getTopLearningPaths(userId));
    }
}

@lombok.Data
class ReviewFlashcardRequest {
    private int rating;
}
