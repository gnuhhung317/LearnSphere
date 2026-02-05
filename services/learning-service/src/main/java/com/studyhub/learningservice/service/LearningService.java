package com.studyhub.learningservice.service;

import com.studyhub.learningservice.dto.AddResourceRequest;
import com.studyhub.learningservice.dto.CreateLearningSpaceRequest;
import com.studyhub.learningservice.dto.DailyGoalDto;
import com.studyhub.learningservice.dto.DailyGoalProgressDto;
import com.studyhub.learningservice.dto.FlashcardDeckDto;
import com.studyhub.learningservice.dto.FlashcardDto;
import com.studyhub.learningservice.dto.LearningSpaceDto;
import com.studyhub.learningservice.dto.ProgressDashboardDto;
import com.studyhub.learningservice.dto.QuizDto;
import com.studyhub.learningservice.dto.ResourceDto;
import com.studyhub.learningservice.dto.StudyFlashcardDto;
import com.studyhub.learningservice.dto.StudyStatsDto;
import com.studyhub.learningservice.dto.UpdateLearningSpaceRequest;

import java.util.List;

public interface LearningService {
        LearningSpaceDto createLearningSpace(String userId, CreateLearningSpaceRequest request);

        LearningSpaceDto getLearningSpace(Long learningSpaceId);

        LearningSpaceDto updateLearningSpace(Long id, String userId, UpdateLearningSpaceRequest request);

        List<LearningSpaceDto> getUserLearningSpaces(String userId);

        ResourceDto addResource(Long learningSpaceId, AddResourceRequest request);

        ResourceDto updateResource(Long learningSpaceId, Long resourceId,
                        com.studyhub.learningservice.dto.UpdateResourceRequest request);

        void deleteLearningSpace(Long learningSpaceId, String userId);

        void deleteResource(Long learningSpaceId, Long resourceId, String userId);

        List<LearningSpaceDto> searchLearningSpaces(String query, String userId);

        List<LearningSpaceDto> getRecentLearningSpaces(String userId);

        // Quiz & Flashcard methods
        QuizDto createQuiz(Long resourceId, QuizDto quizDto);

        List<QuizDto> getQuizzesByResource(Long resourceId);

        FlashcardDeckDto createFlashcardDeck(Long resourceId, FlashcardDeckDto deckDto);

        List<FlashcardDeckDto> getFlashcardDecksByResource(Long resourceId);

        List<FlashcardDto> getDueFlashcards(Long deckId);

        FlashcardDto reviewFlashcard(Long flashcardId, int rating);

        long countDueFlashcards(String userId);

        // AI / Bulk Operations
        LearningSpaceDto createCourseFromStructure(String userId,
                        com.studyhub.learningservice.dto.CreateCourseStructureRequest request);

        // ============== Study Mode Methods ==============

        /**
         * Get all due flashcards for a user across all learning spaces.
         */
        List<StudyFlashcardDto> getAllDueFlashcards(String userId);

        /**
         * Get study statistics for a user (streak, cards reviewed, etc.)
         */
        StudyStatsDto getStudyStats(String userId);

        /**
         * Review a flashcard and update study stats (streak, counts).
         * This is the unified review endpoint for Study Mode.
         */
        FlashcardDto reviewFlashcardWithStats(String userId, Long flashcardId, int rating);

        // ============== Progress Dashboard Methods ==============

        /**
         * Get comprehensive progress dashboard data including heatmap, stats, and
         * trends.
         */
        ProgressDashboardDto getProgressDashboard(String userId);

        // ============== Daily Goals Methods ==============

        /**
         * Get user's daily goals configuration.
         */
        DailyGoalDto getDailyGoals(String userId);

        /**
         * Update user's daily goals configuration.
         */
        DailyGoalDto updateDailyGoals(String userId, DailyGoalDto goalsDto);

        /**
         * Get today's progress towards daily goals.
         */
        DailyGoalProgressDto getDailyGoalProgress(String userId);

        // ============== Leaderboard Methods ==============

        /**
         * Get the weekly leaderboard.
         */
        com.studyhub.learningservice.dto.LeaderboardDto getLeaderboard(String userId);

        // Profile & Achievements
        java.util.List<com.studyhub.learningservice.dto.AchievementDto> getAchievements(String userId);

        java.util.List<com.studyhub.learningservice.dto.LearningSpaceDto> getTopLearningPaths(String userId);
}
