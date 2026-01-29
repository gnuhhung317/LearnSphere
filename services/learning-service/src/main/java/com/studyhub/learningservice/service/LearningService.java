package com.studyhub.learningservice.service;

import com.studyhub.learningservice.dto.AddResourceRequest;
import com.studyhub.learningservice.dto.CreateLearningSpaceRequest;
import com.studyhub.learningservice.dto.FlashcardDeckDto;
import com.studyhub.learningservice.dto.LearningSpaceDto;
import com.studyhub.learningservice.dto.QuizDto;
import com.studyhub.learningservice.dto.ResourceDto;
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

        long countDueFlashcards(String userId);

        // AI / Bulk Operations
        LearningSpaceDto createCourseFromStructure(String userId,
                        com.studyhub.learningservice.dto.CreateCourseStructureRequest request);
}
