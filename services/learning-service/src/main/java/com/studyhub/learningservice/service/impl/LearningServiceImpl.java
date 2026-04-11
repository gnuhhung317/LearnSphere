package com.studyhub.learningservice.service.impl;

import com.studyhub.learningservice.domain.Resource;
import com.studyhub.learningservice.domain.LearningSpace;
import com.studyhub.learningservice.domain.Section;
import com.studyhub.learningservice.domain.StudyStats;
import com.studyhub.learningservice.domain.StudyHistory;
import com.studyhub.learningservice.domain.Flashcard;
import com.studyhub.learningservice.dto.*;
import com.studyhub.learningservice.repository.ResourceRepository;
import com.studyhub.learningservice.repository.LearningSpaceRepository;
import com.studyhub.learningservice.repository.SectionRepository;
import com.studyhub.learningservice.repository.StudyStatsRepository;
import com.studyhub.learningservice.repository.StudyHistoryRepository;
import com.studyhub.learningservice.service.LearningService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.kafka.core.KafkaTemplate;
import com.studyhub.learningservice.event.LearningSpaceCreatedEvent;
import com.studyhub.learningservice.event.LearningSpaceDeletedEvent;
import com.studyhub.learningservice.event.ResourceAddedEvent;
import com.studyhub.learningservice.event.ResourceDeletedEvent;
import com.studyhub.learningservice.dto.UpdateResourceRequest;
import com.studyhub.learningservice.dto.UpdateLearningSpaceRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LearningServiceImpl implements LearningService {

        private final LearningSpaceRepository learningSpaceRepository;
        private final SectionRepository sectionRepository;
        private final ResourceRepository resourceRepository;
        private final com.studyhub.learningservice.repository.QuizRepository quizRepository;
        private final com.studyhub.learningservice.repository.FlashcardDeckRepository flashcardDeckRepository;
        private final com.studyhub.learningservice.repository.FlashcardRepository flashcardRepository;
        private final StudyStatsRepository studyStatsRepository;
        private final StudyHistoryRepository studyHistoryRepository;
        private final com.studyhub.learningservice.repository.DailyGoalRepository dailyGoalRepository;
        private final KafkaTemplate<String, Object> kafkaTemplate;

        @Override
        public LearningSpaceDto createLearningSpace(String userId, CreateLearningSpaceRequest request) {
                LearningSpace learningSpace = LearningSpace.builder()
                                .title(request.getTitle())
                                .description(request.getDescription())
                                .userId(userId)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                // Create a default section
                Section defaultSection = Section.builder()
                                .title("General")
                                .learningSpace(learningSpace)
                                .orderIndex(0)
                                .build();

                learningSpace.getSections().add(defaultSection);

                LearningSpace saved = learningSpaceRepository.save(learningSpace);

                // Publish event
                try {
                        LearningSpaceCreatedEvent event = LearningSpaceCreatedEvent.builder()
                                        .id(saved.getId())
                                        .title(saved.getTitle())
                                        .description(saved.getDescription())
                                        .userId(saved.getUserId())
                                        .createdAt(saved.getCreatedAt())
                                        .build();
                        kafkaTemplate.send("learning.space.created", event);
                } catch (Exception e) {
                        e.printStackTrace();
                }

                return mapToDto(saved);
        }

        @Override
        public LearningSpaceDto getLearningSpace(Long learningSpaceId) {
                LearningSpace learningSpace = learningSpaceRepository.findById(learningSpaceId)
                                .orElseThrow(() -> new RuntimeException("Learning Space not found"));
                return mapToDto(learningSpace);
        }

        @Override
        public List<LearningSpaceDto> getUserLearningSpaces(String userId) {
                return learningSpaceRepository.findByUserId(userId).stream()
                                .map(this::mapToDto)
                                .collect(Collectors.toList());
        }

        @Override
        public ResourceDto addResource(Long learningSpaceId, AddResourceRequest request) {
                LearningSpace learningSpace = learningSpaceRepository.findById(learningSpaceId)
                                .orElseThrow(() -> new RuntimeException("Learning Space not found"));

                Section section;
                if (request.getSectionId() != null) {
                        section = sectionRepository.findById(request.getSectionId())
                                        .orElseThrow(() -> new RuntimeException("Section not found"));
                        if (!section.getLearningSpace().getId().equals(learningSpaceId)) {
                                throw new RuntimeException("Section does not belong to this learning space");
                        }
                } else {
                        // Default to the first section if not specified
                        if (learningSpace.getSections().isEmpty()) {
                                section = Section.builder().title("General").learningSpace(learningSpace).orderIndex(0)
                                                .build();
                                section = sectionRepository.save(section);
                        } else {
                                section = learningSpace.getSections().get(0);
                        }
                }

                Resource resource = Resource.builder()
                                .title(request.getTitle() == null || request.getTitle().isBlank() ? "Untitled"
                                                : request.getTitle())
                                .type(request.getType())
                                .url(request.getUrl()) // For FILE, this might be fileId or path
                                .description(request.getDescription())
                                .section(section)
                                .orderIndex(section.getResources().size())
                                .isCompleted(false)
                                .status("PENDING")
                                .build();

                Resource savedResource = resourceRepository.save(resource);

                // Publish event
                try {
                        ResourceAddedEvent event = ResourceAddedEvent.builder()
                                        .resourceId(savedResource.getId())
                                        .learningSpaceId(learningSpaceId)
                                        .title(savedResource.getTitle())
                                        .type(savedResource.getType().name())
                                        .url(savedResource.getUrl())
                                        .userId(learningSpace.getUserId())
                                        .build();
                        kafkaTemplate.send("learning.resource.added", event);
                } catch (Exception e) {
                        // Log error but don't fail the request
                        e.printStackTrace();
                }

                return mapToDto(savedResource);
        }

        @Override
        public ResourceDto updateResource(Long learningSpaceId, Long resourceId, UpdateResourceRequest request) {
                Resource resource = resourceRepository.findById(resourceId)
                                .orElseThrow(() -> new RuntimeException("Resource not found"));

                if (!resource.getSection().getLearningSpace().getId().equals(learningSpaceId)) {
                        throw new RuntimeException("Resource does not belong to this learning space");
                }

                if (request.getTitle() != null) {
                        resource.setTitle(request.getTitle());
                }
                if (request.getDescription() != null) {
                        resource.setDescription(request.getDescription());
                }
                if (request.getIsCompleted() != null) {
                        resource.setCompleted(request.getIsCompleted());
                }
                if (request.getStatus() != null) {
                        resource.setStatus(request.getStatus());
                }

                Resource saved = resourceRepository.save(resource);
                return mapToDto(saved);
        }

        @Override
        public LearningSpaceDto updateLearningSpace(Long id, String userId, UpdateLearningSpaceRequest request) {
                LearningSpace learningSpace = learningSpaceRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Learning Space not found"));

                if (!learningSpace.getUserId().equals(userId)) {
                        throw new RuntimeException("Not authorized to update this learning space");
                }

                if (request.getTitle() != null) {
                        learningSpace.setTitle(request.getTitle());
                }
                if (request.getDescription() != null) {
                        learningSpace.setDescription(request.getDescription());
                }
                learningSpace.setUpdatedAt(LocalDateTime.now());

                LearningSpace saved = learningSpaceRepository.save(learningSpace);
                return mapToDto(saved);
        }

        @Override
        public void deleteLearningSpace(Long learningSpaceId, String userId) {
                LearningSpace learningSpace = learningSpaceRepository.findById(learningSpaceId)
                                .orElseThrow(() -> new RuntimeException("Learning Space not found"));
                if (!learningSpace.getUserId().equals(userId)) {
                        throw new RuntimeException("Not authorized to delete this learning space");
                }
                learningSpaceRepository.delete(learningSpace);

                // Publish delete event
                try {
                        LearningSpaceDeletedEvent event = LearningSpaceDeletedEvent.builder()
                                        .learningSpaceId(learningSpaceId)
                                        .build();
                        kafkaTemplate.send("learning.space.deleted", event);
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        @Override
        public void deleteResource(Long learningSpaceId, Long resourceId, String userId) {
                Resource resource = resourceRepository.findById(resourceId)
                                .orElseThrow(() -> new RuntimeException("Resource not found"));

                LearningSpace learningSpace = resource.getSection().getLearningSpace();
                if (!learningSpace.getId().equals(learningSpaceId)) {
                        throw new RuntimeException("Resource does not belong to this learning space");
                }

                if (!learningSpace.getUserId().equals(userId)) {
                        throw new RuntimeException("Not authorized to delete this resource");
                }

                resourceRepository.delete(resource);

                // Publish delete event
                try {
                        ResourceDeletedEvent event = ResourceDeletedEvent.builder()
                                        .resourceId(resourceId)
                                        .learningSpaceId(learningSpaceId)
                                        .type(resource.getType().name())
                                        .url(resource.getUrl())
                                        .build();
                        kafkaTemplate.send("learning.resource.deleted", event);
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        @Override
        public List<LearningSpaceDto> searchLearningSpaces(String query, String userId) {
                return learningSpaceRepository.findByTitleContainingIgnoreCaseAndUserId(query, userId).stream()
                                .map(this::mapToDto)
                                .collect(Collectors.toList());
        }

        @Override
        public List<LearningSpaceDto> getRecentLearningSpaces(String userId) {
                return learningSpaceRepository.findTop5ByUserIdOrderByUpdatedAtDesc(userId).stream()
                                .map(this::mapToDto)
                                .collect(Collectors.toList());
        }

        private LearningSpaceDto mapToDto(LearningSpace learningSpace) {
                return LearningSpaceDto.builder()
                                .id(learningSpace.getId())
                                .title(learningSpace.getTitle())
                                .description(learningSpace.getDescription())
                                .userId(learningSpace.getUserId())
                                .createdAt(learningSpace.getCreatedAt())
                                .updatedAt(learningSpace.getUpdatedAt())
                                .sections(learningSpace.getSections().stream().map(this::mapSection)
                                                .collect(Collectors.toList()))
                                .build();
        }

        private SectionDto mapSection(Section section) {
                return SectionDto.builder()
                                .id(section.getId())
                                .title(section.getTitle())
                                .orderIndex(section.getOrderIndex())
                                .resources(section.getResources().stream().map(this::mapToDto)
                                                .collect(Collectors.toList()))
                                .build();
        }

        private ResourceDto mapToDto(Resource resource) {
                return ResourceDto.builder()
                                .id(resource.getId())
                                .title(resource.getTitle())
                                .type(resource.getType())
                                .url(resource.getUrl())
                                .description(resource.getDescription())
                                .orderIndex(resource.getOrderIndex())
                                .isCompleted(resource.isCompleted())
                                .status(resource.getStatus())
                                .build();
        }

        @Override
        public QuizDto createQuiz(Long resourceId, QuizDto quizDto) {
                Resource resource = resourceRepository.findById(resourceId)
                                .orElseThrow(() -> new RuntimeException("Resource not found"));

                com.studyhub.learningservice.domain.Quiz quiz = com.studyhub.learningservice.domain.Quiz.builder()
                                .title(quizDto.getTitle())
                                .description(quizDto.getDescription())
                                .resource(resource)
                                .build();

                if (quizDto.getQuestions() != null) {
                        List<com.studyhub.learningservice.domain.Question> questions = quizDto.getQuestions().stream()
                                        .map(qDto -> com.studyhub.learningservice.domain.Question.builder()
                                                        .quiz(quiz)
                                                        .text(qDto.getText())
                                                        .type(qDto.getType())
                                                        .options(qDto.getOptions())
                                                        .correctAnswer(qDto.getCorrectAnswer())
                                                        .explanation(qDto.getExplanation())
                                                        .build())
                                        .collect(Collectors.toList());
                        quiz.setQuestions(questions);
                }

                com.studyhub.learningservice.domain.Quiz savedQuiz = quizRepository.save(quiz);
                return mapToQuizDto(savedQuiz);
        }

        @Override
        public List<QuizDto> getQuizzesByResource(Long resourceId) {
                return quizRepository.findByResourceId(resourceId).stream()
                                .map(this::mapToQuizDto)
                                .collect(Collectors.toList());
        }

        @Override
        public FlashcardDeckDto createFlashcardDeck(Long resourceId, FlashcardDeckDto deckDto) {
                Resource resource = resourceRepository.findById(resourceId)
                                .orElseThrow(() -> new RuntimeException("Resource not found"));

                com.studyhub.learningservice.domain.FlashcardDeck deck = com.studyhub.learningservice.domain.FlashcardDeck
                                .builder()
                                .title(deckDto.getTitle())
                                .resource(resource)
                                .build();

                if (deckDto.getFlashcards() != null) {
                        List<com.studyhub.learningservice.domain.Flashcard> flashcards = deckDto.getFlashcards()
                                        .stream()
                                        .map(fDto -> com.studyhub.learningservice.domain.Flashcard.builder()
                                                        .deck(deck)
                                                        .front(fDto.getFront())
                                                        .back(fDto.getBack())
                                                        .build())
                                        .collect(Collectors.toList());
                        deck.setFlashcards(flashcards);
                }

                com.studyhub.learningservice.domain.FlashcardDeck savedDeck = flashcardDeckRepository.save(deck);
                return mapToFlashcardDeckDto(savedDeck);
        }

        @Override
        public List<FlashcardDeckDto> getFlashcardDecksByResource(Long resourceId) {
                return flashcardDeckRepository.findByResourceId(resourceId).stream()
                                .map(this::mapToFlashcardDeckDto)
                                .collect(Collectors.toList());
        }

        @Override
        public List<FlashcardDto> getDueFlashcards(Long deckId) {
                return flashcardDeckRepository.findById(deckId)
                                .map(deck -> deck.getFlashcards().stream()
                                                .filter(f -> f.getNextReviewDate() == null
                                                                || f.getNextReviewDate().isBefore(LocalDateTime.now()))
                                                .map(f -> FlashcardDto.builder()
                                                                .id(f.getId())
                                                                .front(f.getFront())
                                                                .back(f.getBack())
                                                                .build())
                                                .collect(Collectors.toList()))
                                .orElseThrow(() -> new RuntimeException("Deck not found"));
        }

        @Override
        public FlashcardDto reviewFlashcard(Long flashcardId, int rating) {
                // Rating: 1 = Again, 2 = Hard, 3 = Good, 4 = Easy
                com.studyhub.learningservice.domain.Flashcard flashcard = flashcardRepository.findById(flashcardId)
                                .orElseThrow(() -> new RuntimeException("Flashcard not found"));

                // SM-2 Algorithm
                if (rating < 3) {
                        flashcard.setRepetitions(0);
                        flashcard.setIntervalDays(1);
                } else {
                        if (flashcard.getRepetitions() == 0) {
                                flashcard.setIntervalDays(1);
                        } else if (flashcard.getRepetitions() == 1) {
                                flashcard.setIntervalDays(6);
                        } else {
                                flashcard.setIntervalDays((int) Math
                                                .round(flashcard.getIntervalDays() * flashcard.getEaseFactor()));
                        }

                        flashcard.setRepetitions(flashcard.getRepetitions() + 1);

                        // Update Ease Factor
                        double newEase = flashcard.getEaseFactor()
                                        + (0.1 - (5 - rating) * (0.08 + (5 - rating) * 0.02));
                        if (newEase < 1.3)
                                newEase = 1.3;
                        flashcard.setEaseFactor(newEase);
                }

                flashcard.setNextReviewDate(LocalDateTime.now().plusDays(flashcard.getIntervalDays()));

                flashcardRepository.save(flashcard);

                return FlashcardDto.builder()
                                .id(flashcard.getId())
                                .front(flashcard.getFront())
                                .back(flashcard.getBack())
                                .build();
        }

        @Override
        public long countDueFlashcards(String userId) {
                return flashcardRepository.countDueFlashcards(userId, LocalDateTime.now());
        }

        @Override
        @Transactional
        public LearningSpaceDto createCourseFromStructure(String userId,
                        com.studyhub.learningservice.dto.CreateCourseStructureRequest request) {
                LearningSpace learningSpace = LearningSpace.builder()
                                .title(request.getTitle())
                                .description(request.getDescription())
                                .userId(userId)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                if (request.getSections() != null) {
                        int sectionOrder = 0;
                        for (com.studyhub.learningservice.dto.CreateCourseStructureRequest.SectionRequest secReq : request
                                        .getSections()) {
                                Section section = Section.builder()
                                                .title(secReq.getTitle())
                                                .learningSpace(learningSpace)
                                                .orderIndex(sectionOrder++)
                                                .build();

                                if (secReq.getResources() != null) {
                                        int resourceOrder = 0;
                                        for (com.studyhub.learningservice.dto.CreateCourseStructureRequest.ResourceRequest resReq : secReq
                                                        .getResources()) {
                                                Resource resource = Resource.builder()
                                                                .title(resReq.getTitle())
                                                                .type(resReq.getType())
                                                                .url(resReq.getUrl())
                                                                .description(resReq.getDescription())
                                                                .section(section)
                                                                .orderIndex(resourceOrder++)
                                                                .isCompleted(false)
                                                                .status("PENDING")
                                                                .build();
                                                section.getResources().add(resource);
                                        }
                                }
                                learningSpace.getSections().add(section);
                        }
                }

                LearningSpace saved = learningSpaceRepository.save(learningSpace);

                // Publish event (Simplified for now, just the main space event)
                try {
                        LearningSpaceCreatedEvent event = LearningSpaceCreatedEvent.builder()
                                        .id(saved.getId())
                                        .title(saved.getTitle())
                                        .description(saved.getDescription())
                                        .userId(saved.getUserId())
                                        .createdAt(saved.getCreatedAt())
                                        .build();
                        kafkaTemplate.send("learning.space.created", event);
                } catch (Exception e) {
                        e.printStackTrace();
                }

                return mapToDto(saved);
        }

        private QuizDto mapToQuizDto(com.studyhub.learningservice.domain.Quiz quiz) {
                return QuizDto.builder()
                                .id(quiz.getId())
                                .title(quiz.getTitle())
                                .description(quiz.getDescription())
                                .resourceId(quiz.getResource().getId())
                                .questions(quiz.getQuestions().stream()
                                                .map(q -> QuestionDto.builder()
                                                                .id(q.getId())
                                                                .text(q.getText())
                                                                .type(q.getType())
                                                                .options(q.getOptions())
                                                                .correctAnswer(q.getCorrectAnswer())
                                                                .explanation(q.getExplanation())
                                                                .build())
                                                .collect(Collectors.toList()))
                                .build();
        }

        private FlashcardDeckDto mapToFlashcardDeckDto(com.studyhub.learningservice.domain.FlashcardDeck deck) {
                return FlashcardDeckDto.builder()
                                .id(deck.getId())
                                .title(deck.getTitle())
                                .resourceId(deck.getResource().getId())
                                .flashcards(deck.getFlashcards().stream()
                                                .map(f -> FlashcardDto.builder()
                                                                .id(f.getId())
                                                                .front(f.getFront())
                                                                .back(f.getBack())
                                                                .build())
                                                .collect(Collectors.toList()))
                                .build();
        }

        // ==================== Study Mode Methods ====================

        @Override
        @Transactional(readOnly = true)
        public List<StudyFlashcardDto> getAllDueFlashcards(String userId) {
                List<Flashcard> dueCards = flashcardRepository.findDueFlashcardsByUserId(
                                userId, LocalDateTime.now());

                return dueCards.stream()
                                .map(f -> {
                                        var deck = f.getDeck();
                                        var resource = deck.getResource();
                                        var section = resource.getSection();
                                        var space = section.getLearningSpace();

                                        return StudyFlashcardDto.builder()
                                                        .id(f.getId())
                                                        .front(f.getFront())
                                                        .back(f.getBack())
                                                        .deckId(deck.getId())
                                                        .deckTitle(deck.getTitle())
                                                        .learningSpaceId(space.getId())
                                                        .learningSpaceTitle(space.getTitle())
                                                        .repetitions(f.getRepetitions())
                                                        .intervalDays(f.getIntervalDays())
                                                        .build();
                                })
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public StudyStatsDto getStudyStats(String userId) {
                long dueCount = flashcardRepository.countDueFlashcards(userId, LocalDateTime.now());

                StudyStats stats = studyStatsRepository.findByUserId(userId)
                                .orElse(StudyStats.builder()
                                                .userId(userId)
                                                .currentStreak(0)
                                                .longestStreak(0)
                                                .totalCardsReviewed(0L)
                                                .cardsReviewedToday(0)
                                                .build());

                // Check if we need to reset today's count (new day)
                LocalDate today = LocalDate.now();
                int cardsToday = stats.getTodayDate() != null && stats.getTodayDate().equals(today)
                                ? stats.getCardsReviewedToday()
                                : 0;

                // Check if streak should be reset (missed a day)
                int currentStreak = stats.getCurrentStreak();
                if (stats.getLastStudyDate() != null) {
                        LocalDate lastStudy = stats.getLastStudyDate();
                        if (!lastStudy.equals(today) && !lastStudy.equals(today.minusDays(1))) {
                                // Streak broken
                                currentStreak = 0;
                        }
                }

                return StudyStatsDto.builder()
                                .totalDueCards(dueCount)
                                .cardsReviewedToday(cardsToday)
                                .currentStreak(currentStreak)
                                .longestStreak(stats.getLongestStreak())
                                .totalCardsReviewed(stats.getTotalCardsReviewed())
                                .lastStudyDate(stats.getLastStudyDate())
                                .build();
        }

        @Override
        public FlashcardDto reviewFlashcardWithStats(String userId, Long flashcardId, int rating) {
                // First, perform the regular review
                FlashcardDto result = reviewFlashcard(flashcardId, rating);

                // Calculate XP based on rating
                int xpEarned = rating == 4 ? 20 : (rating == 3 ? 15 : 10);

                // Update study stats
                StudyStats stats = studyStatsRepository.findByUserId(userId)
                                .orElseGet(() -> StudyStats.builder()
                                                .userId(userId)
                                                .currentStreak(0)
                                                .longestStreak(0)
                                                .totalCardsReviewed(0L)
                                                .cardsReviewedToday(0)
                                                .build());

                stats.recordStudySession();
                studyStatsRepository.save(stats);

                // Record study history for heatmap
                LocalDate today = LocalDate.now();
                StudyHistory history = studyHistoryRepository.findByUserIdAndStudyDate(userId, today)
                                .orElseGet(() -> StudyHistory.builder()
                                                .userId(userId)
                                                .studyDate(today)
                                                .cardsReviewed(0)
                                                .quizzesCompleted(0)
                                                .xpEarned(0)
                                                .build());

                history.addCardReview(xpEarned);
                studyHistoryRepository.save(history);

                return result;
        }

        // ==================== Progress Dashboard Methods ====================

        @Override
        @Transactional(readOnly = true)
        public ProgressDashboardDto getProgressDashboard(String userId) {
                LocalDate today = LocalDate.now();
                LocalDate yearAgo = today.minusYears(1);
                LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);

                // Get study stats
                StudyStats stats = studyStatsRepository.findByUserId(userId)
                                .orElse(StudyStats.builder()
                                                .currentStreak(0)
                                                .longestStreak(0)
                                                .build());

                // Get study history for heatmap (last 365 days)
                List<StudyHistory> historyList = studyHistoryRepository.findByUserIdAndDateRange(
                                userId, yearAgo, today);

                // Create a map for quick lookup
                Map<LocalDate, StudyHistory> historyMap = historyList.stream()
                                .collect(Collectors.toMap(StudyHistory::getStudyDate, h -> h));

                // Generate heatmap data
                List<StudyDayDto> heatmapData = new ArrayList<>();
                int maxCards = historyList.stream()
                                .mapToInt(StudyHistory::getCardsReviewed)
                                .max()
                                .orElse(1);

                for (LocalDate date = yearAgo; !date.isAfter(today); date = date.plusDays(1)) {
                        StudyHistory h = historyMap.get(date);
                        int cards = h != null ? h.getCardsReviewed() : 0;
                        int quizzes = h != null ? h.getQuizzesCompleted() : 0;
                        int xp = h != null ? h.getXpEarned() : 0;

                        // Calculate level (0-4) based on activity
                        int level = 0;
                        if (cards > 0) {
                                double ratio = (double) cards / maxCards;
                                if (ratio >= 0.75)
                                        level = 4;
                                else if (ratio >= 0.5)
                                        level = 3;
                                else if (ratio >= 0.25)
                                        level = 2;
                                else
                                        level = 1;
                        }

                        heatmapData.add(StudyDayDto.builder()
                                        .date(date)
                                        .cardsReviewed(cards)
                                        .quizzesCompleted(quizzes)
                                        .xpEarned(xp)
                                        .level(level)
                                        .build());
                }

                // Calculate weekly stats (last 12 weeks)
                List<WeeklyStatsDto> weeklyStats = new ArrayList<>();
                DateTimeFormatter weekFormatter = DateTimeFormatter.ofPattern("MMM d");

                for (int i = 11; i >= 0; i--) {
                        LocalDate wStart = weekStart.minusWeeks(i);
                        LocalDate wEnd = wStart.plusDays(6);

                        int weekCards = 0;
                        int weekQuizzes = 0;
                        int weekXp = 0;
                        int studyDays = 0;

                        for (LocalDate d = wStart; !d.isAfter(wEnd) && !d.isAfter(today); d = d.plusDays(1)) {
                                StudyHistory h = historyMap.get(d);
                                if (h != null) {
                                        weekCards += h.getCardsReviewed();
                                        weekQuizzes += h.getQuizzesCompleted();
                                        weekXp += h.getXpEarned();
                                        if (h.getCardsReviewed() > 0)
                                                studyDays++;
                                }
                        }

                        String weekLabel = wStart.format(weekFormatter) + "-" + wEnd.format(weekFormatter);
                        weeklyStats.add(WeeklyStatsDto.builder()
                                        .weekLabel(weekLabel)
                                        .cardsReviewed(weekCards)
                                        .quizzesCompleted(weekQuizzes)
                                        .xpEarned(weekXp)
                                        .studyDays(studyDays)
                                        .build());
                }

                // Get this week's stats
                List<StudyHistory> thisWeek = studyHistoryRepository.findByUserIdAndDateRange(
                                userId, weekStart, today);
                int weekCardsReviewed = thisWeek.stream().mapToInt(StudyHistory::getCardsReviewed).sum();
                int weekQuizzesCompleted = thisWeek.stream().mapToInt(StudyHistory::getQuizzesCompleted).sum();
                int weekXpEarned = thisWeek.stream().mapToInt(StudyHistory::getXpEarned).sum();

                // Get today's stats
                StudyHistory todayHistory = historyMap.get(today);
                int todayCards = todayHistory != null ? todayHistory.getCardsReviewed() : 0;
                int todayQuizzes = todayHistory != null ? todayHistory.getQuizzesCompleted() : 0;

                // Get totals
                long totalCards = studyHistoryRepository.getTotalCardsReviewed(userId);
                long totalQuizzes = studyHistoryRepository.getTotalQuizzesCompleted(userId);
                long totalXp = studyHistoryRepository.getTotalXpEarned(userId);
                long totalDays = studyHistoryRepository.getTotalStudyDays(userId);

                return ProgressDashboardDto.builder()
                                .currentStreak(stats.getCurrentStreak())
                                .longestStreak(stats.getLongestStreak())
                                .totalCardsReviewed(totalCards)
                                .totalQuizzesCompleted(totalQuizzes)
                                .totalXpEarned(totalXp)
                                .totalStudyDays(totalDays)
                                .weekCardsReviewed(weekCardsReviewed)
                                .weekQuizzesCompleted(weekQuizzesCompleted)
                                .weekXpEarned(weekXpEarned)
                                .todayCardsReviewed(todayCards)
                                .todayQuizzesCompleted(todayQuizzes)
                                .heatmapData(heatmapData)
                                .weeklyStats(weeklyStats)
                                .build();
        }

        // ==================== Daily Goals Methods ====================

        @Override
        @Transactional(readOnly = true)
        public DailyGoalDto getDailyGoals(String userId) {
                com.studyhub.learningservice.domain.DailyGoal goals = dailyGoalRepository.findByUserId(userId)
                                .orElseGet(() -> com.studyhub.learningservice.domain.DailyGoal.builder()
                                                .userId(userId)
                                                .cardsGoal(20)
                                                .quizzesGoal(2)
                                                .xpGoal(100)
                                                .focusMinutesGoal(60)
                                                .remindersEnabled(true)
                                                .reminderHour(20)
                                                .build());

                return DailyGoalDto.builder()
                                .cardsGoal(goals.getCardsGoal())
                                .quizzesGoal(goals.getQuizzesGoal())
                                .xpGoal(goals.getXpGoal())
                                .focusMinutesGoal(goals.getFocusMinutesGoal())
                                .remindersEnabled(goals.getRemindersEnabled())
                                .reminderHour(goals.getReminderHour())
                                .build();
        }

        @Override
        public DailyGoalDto updateDailyGoals(String userId, DailyGoalDto goalsDto) {
                com.studyhub.learningservice.domain.DailyGoal goals = dailyGoalRepository.findByUserId(userId)
                                .orElseGet(() -> com.studyhub.learningservice.domain.DailyGoal.builder()
                                                .userId(userId)
                                                .build());

                goals.setCardsGoal(goalsDto.getCardsGoal());
                goals.setQuizzesGoal(goalsDto.getQuizzesGoal());
                goals.setXpGoal(goalsDto.getXpGoal());
                goals.setFocusMinutesGoal(goalsDto.getFocusMinutesGoal());
                goals.setRemindersEnabled(goalsDto.isRemindersEnabled());
                goals.setReminderHour(goalsDto.getReminderHour());

                dailyGoalRepository.save(goals);

                return goalsDto;
        }

        @Override
        @Transactional(readOnly = true)
        public DailyGoalProgressDto getDailyGoalProgress(String userId) {
                // Get goals
                DailyGoalDto goals = getDailyGoals(userId);

                // Get today's history
                LocalDate today = LocalDate.now();
                StudyHistory todayHistory = studyHistoryRepository.findByUserIdAndStudyDate(userId, today)
                                .orElse(null);

                int cardsCompleted = todayHistory != null ? todayHistory.getCardsReviewed() : 0;
                int quizzesCompleted = todayHistory != null ? todayHistory.getQuizzesCompleted() : 0;
                int xpEarned = todayHistory != null ? todayHistory.getXpEarned() : 0;
                int focusMinutes = todayHistory != null ? todayHistory.getStudyMinutes() : 0;

                // Calculate percentages
                int cardsPercentage = goals.getCardsGoal() > 0
                                ? Math.min(100, (cardsCompleted * 100) / goals.getCardsGoal())
                                : 100;
                int quizzesPercentage = goals.getQuizzesGoal() > 0
                                ? Math.min(100, (quizzesCompleted * 100) / goals.getQuizzesGoal())
                                : 100;
                int xpPercentage = goals.getXpGoal() > 0
                                ? Math.min(100, (xpEarned * 100) / goals.getXpGoal())
                                : 100;
                int focusPercentage = goals.getFocusMinutesGoal() > 0
                                ? Math.min(100, (focusMinutes * 100) / goals.getFocusMinutesGoal())
                                : 100;

                int overallPercentage = (cardsPercentage + quizzesPercentage + xpPercentage + focusPercentage) / 4;
                boolean goalsMet = cardsPercentage >= 100 && quizzesPercentage >= 100
                                && xpPercentage >= 100 && focusPercentage >= 100;

                return DailyGoalProgressDto.builder()
                                .cardsGoal(goals.getCardsGoal())
                                .quizzesGoal(goals.getQuizzesGoal())
                                .xpGoal(goals.getXpGoal())
                                .focusMinutesGoal(goals.getFocusMinutesGoal())
                                .cardsCompleted(cardsCompleted)
                                .quizzesCompleted(quizzesCompleted)
                                .xpEarned(xpEarned)
                                .focusMinutesCompleted(focusMinutes)
                                .cardsPercentage(cardsPercentage)
                                .quizzesPercentage(quizzesPercentage)
                                .xpPercentage(xpPercentage)
                                .focusPercentage(focusPercentage)
                                .overallPercentage(overallPercentage)
                                .goalsMet(goalsMet)
                                .build();
        }

        // ==================== Leaderboard Methods ====================

        @Override
        public com.studyhub.learningservice.dto.LeaderboardDto getLeaderboard(String userId) {
                // Get start of week
                LocalDate today = LocalDate.now();
                LocalDate startOfWeek = today.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);

                // Get top 50 users for this week
                List<com.studyhub.learningservice.repository.LeaderboardProjection> topUsers = studyHistoryRepository
                                .getTopUsersByXp(startOfWeek);

                List<com.studyhub.learningservice.dto.LeaderboardEntryDto> entries = new ArrayList<>();
                int rank = 1;
                com.studyhub.learningservice.dto.LeaderboardEntryDto currentUserEntry = null;

                for (com.studyhub.learningservice.repository.LeaderboardProjection projection : topUsers) {
                        boolean isCurrentUser = projection.getUserId().equals(userId);
                        String username = "User_" + projection.getUserId().substring(
                                        Math.max(0, projection.getUserId().length() - 4));

                        com.studyhub.learningservice.dto.LeaderboardEntryDto entry = com.studyhub.learningservice.dto.LeaderboardEntryDto
                                        .builder()
                                        .userId(projection.getUserId())
                                        .username(username)
                                        .xp(projection.getTotalXp().intValue())
                                        .rank(rank++)
                                        .isCurrentUser(isCurrentUser)
                                        .avatarUrl("https://api.dicebear.com/7.x/notionists/svg?seed="
                                                        + projection.getUserId())
                                        .build();

                        entries.add(entry);

                        if (isCurrentUser) {
                                currentUserEntry = entry;
                        }
                }

                // If current user not in top 50, calculate their stats separate (simplified for
                // now as 0 or last)
                if (currentUserEntry == null) {
                        Long userWeeklyXp = studyHistoryRepository.findByUserIdAndDateRange(userId, startOfWeek, today)
                                        .stream().mapToLong(StudyHistory::getXpEarned).sum();

                        currentUserEntry = com.studyhub.learningservice.dto.LeaderboardEntryDto.builder()
                                        .userId(userId)
                                        .username("User_" + userId.substring(Math.max(0, userId.length() - 4)))
                                        .xp(userWeeklyXp.intValue())
                                        .rank(topUsers.size() + 1) // Approximation
                                        .isCurrentUser(true)
                                        .avatarUrl("https://api.dicebear.com/7.x/notionists/svg?seed=" + userId)
                                        .build();
                }

                return com.studyhub.learningservice.dto.LeaderboardDto.builder()
                                .topUsers(entries.stream().limit(10).collect(Collectors.toList()))
                                .currentUser(currentUserEntry)
                                .period("WEEKLY")
                                .build();
        }

        // ==================== Profile Methods ====================

        @Override
        public List<com.studyhub.learningservice.dto.AchievementDto> getAchievements(String userId) {
                com.studyhub.learningservice.domain.StudyStats stats = studyStatsRepository.findByUserId(userId)
                                .orElse(com.studyhub.learningservice.domain.StudyStats.builder()
                                                .userId(userId)
                                                .build());
                List<com.studyhub.learningservice.dto.AchievementDto> achievements = new ArrayList<>();

                // 1. First Steps
                achievements.add(com.studyhub.learningservice.dto.AchievementDto.builder()
                                .id("first-steps")
                                .name("First Steps")
                                .description("Review your first flashcard")
                                .icon("flag")
                                .unlockedAt(stats.getTotalCardsReviewed() > 0 ? stats.getCreatedAt().toString() : null)
                                .progress(stats.getTotalCardsReviewed() > 0 ? 1 : 0)
                                .maxProgress(1)
                                .build());

                // 2. Dedicated Student (50 reviews)
                achievements.add(com.studyhub.learningservice.dto.AchievementDto.builder()
                                .id("dedicated-student")
                                .name("Dedicated Student")
                                .description("Review 50 flashcards")
                                .icon("book-open")
                                .unlockedAt(stats.getTotalCardsReviewed() >= 50 ? stats.getUpdatedAt().toString()
                                                : null)
                                .progress(Math.min(stats.getTotalCardsReviewed().intValue(), 50))
                                .maxProgress(50)
                                .build());

                // 3. Streak Master (7 days)
                achievements.add(com.studyhub.learningservice.dto.AchievementDto.builder()
                                .id("streak-master")
                                .name("Streak Master")
                                .description("Achieve a 7-day study streak")
                                .icon("fire")
                                .unlockedAt(stats.getLongestStreak() >= 7 ? stats.getUpdatedAt().toString() : null)
                                .progress(Math.min(stats.getLongestStreak(), 7))
                                .maxProgress(7)
                                .build());

                return achievements;
        }

        @Override
        public List<com.studyhub.learningservice.dto.LearningSpaceDto> getTopLearningPaths(String userId) {
                // Return top learning spaces as "paths", sorted by creation date descending
                return learningSpaceRepository.findByUserId(userId).stream()
                                .map(space -> com.studyhub.learningservice.dto.LearningSpaceDto.builder()
                                                .id(space.getId())
                                                .title(space.getTitle())
                                                .description(space.getDescription())
                                                .userId(space.getUserId())
                                                .createdAt(space.getCreatedAt())
                                                .updatedAt(space.getUpdatedAt())
                                                // Simplified progress logic: if created > 1 day ago call it 10%, else
                                                // 0%
                                                // Real logic would need card counts
                                                .build())
                                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                                .limit(5)
                                .collect(Collectors.toList());
        }
}
