package com.studyhub.learningservice.service.impl;

import com.studyhub.learningservice.domain.Resource;
import com.studyhub.learningservice.domain.LearningSpace;
import com.studyhub.learningservice.domain.Section;
import com.studyhub.learningservice.dto.*;
import com.studyhub.learningservice.repository.ResourceRepository;
import com.studyhub.learningservice.repository.LearningSpaceRepository;
import com.studyhub.learningservice.repository.SectionRepository;
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

import java.time.LocalDateTime;
import java.util.List;
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
}
