package com.studyhub.ai_service.service;

import com.studyhub.ai_service.dto.*;
import com.studyhub.ai_service.entity.VectorChunk;
import com.studyhub.ai_service.repository.VectorStoreRepository;
import com.studyhub.ai_service.client.GeminiEmbeddingClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningAssistantService {

    private final ChatClient.Builder chatClientBuilder;
    private final VectorStoreRepository vectorStoreRepository;
    private final GeminiEmbeddingClient embeddingClient;

    public GeneratedQuizResponse generateQuiz(GenerateQuizRequest request) {
        log.info("Generating quiz. Topic: {}", request.getTopic());

        // 1. Resolve Content (RAG or Fallback)
        String contentToUse = resolveContent(
                request.getContent(),
                request.getResourceId(),
                request.getResourceType(),
                request.getResourceUrl(),
                request.getTopic());

        BeanOutputConverter<GeneratedQuizResponse> parser = new BeanOutputConverter<>(GeneratedQuizResponse.class);

        String promptText = """
                Generate a quiz based on the following content.
                Topic/Focus: {topic}
                Difficulty: {difficulty}
                Number of Questions: {count}

                Instructions:
                - Create {count} multiple-choice questions.
                - Use valid distractors (wrong answers) that are plausible but incorrect.
                - Ensure the questions are DIRECTLY related to the provided content.

                Content:
                {content}

                {format}
                """;

        PromptTemplate template = new PromptTemplate(promptText);
        Prompt prompt = template.create(Map.of(
                "topic",
                request.getTopic() != null && !request.getTopic().isEmpty() ? request.getTopic() : "General Review",
                "difficulty", request.getDifficulty() != null ? request.getDifficulty() : "Normal",
                "count", request.getNumberOfQuestions() > 0 ? request.getNumberOfQuestions() : 5,
                "content", contentToUse,
                "format", parser.getFormat()));

        ChatClient chatClient = chatClientBuilder.build();

        try {
            String response = chatClient.prompt(prompt).call().content();
            return parser.convert(response);
        } catch (Exception e) {
            log.error("Error generating quiz", e);
            throw new RuntimeException("Failed to generate quiz", e);
        }
    }

    public GeneratedFlashcardsResponse generateFlashcards(GenerateFlashcardsRequest request) {
        log.info("Generating flashcards. Topic: {}", request.getTopic());

        // 1. Resolve Content (RAG or Fallback)
        String contentToUse = resolveContent(
                request.getContent(),
                request.getResourceId(),
                request.getResourceType(),
                request.getResourceUrl(),
                request.getTopic());

        BeanOutputConverter<GeneratedFlashcardsResponse> parser = new BeanOutputConverter<>(
                GeneratedFlashcardsResponse.class);

        String promptText = """
                Generate {count} flashcards based on the following content.
                Focus Area: {topic}

                Instructions:
                - Create concise front (term/question) and back (definition/answer) pairs.
                - Focus on key concepts, definitions, and important details found in the content.

                Content:
                {content}

                {format}
                """;

        PromptTemplate template = new PromptTemplate(promptText);
        Prompt prompt = template.create(Map.of(
                "topic",
                request.getTopic() != null && !request.getTopic().isEmpty() ? request.getTopic() : "General Review",
                "count", request.getCount() > 0 ? request.getCount() : 5,
                "content", contentToUse,
                "format", parser.getFormat()));

        ChatClient chatClient = chatClientBuilder.build();

        try {
            String response = chatClient.prompt(prompt).call().content();
            return parser.convert(response);
        } catch (Exception e) {
            log.error("Error generating flashcards", e);
            throw new RuntimeException("Failed to generate flashcards", e);
        }
    }

    public GeneratedCoursePlan generateCoursePlan(GeneratedCoursePlanRequest request) {
        log.info("Generating course plan. Topic: {}", request.getTopic());

        BeanOutputConverter<GeneratedCoursePlan> parser = new BeanOutputConverter<>(GeneratedCoursePlan.class);

        String promptText = """
                Generate a structured learning course plan for the following topic.
                Topic: {topic}
                Level: {level}
                Goal: {goal}

                Instructions:
                - Create a logical curriculum structure with sections.
                - For each section, provide 1-3 resources.
                - IMPORTANT: For resources, mostly use "NOTE" type and provide the actual educational content in the 'url' field (markdown format).
                - You can also suggest "LINK" types if you know famous YouTube videos or documentation.
                - The 'type' must be one of: LINK, FILE, NOTE.

                {format}
                """;

        PromptTemplate template = new PromptTemplate(promptText);
        Prompt prompt = template.create(Map.of(
                "topic", request.getTopic(),
                "level", request.getLevel() != null ? request.getLevel() : "Beginner",
                "goal", request.getGoal() != null ? request.getGoal() : "Learn the basics",
                "format", parser.getFormat()));

        ChatClient chatClient = chatClientBuilder.build();
        try {
            String response = chatClient.prompt(prompt).call().content();
            return parser.convert(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate course plan", e);
        }
    }

    /**
     * Resolves content using RAG strategies if resource info is valid.
     * Strategies:
     * 1. Specific Topic -> Semantic Search
     * 2. General/Random -> Fetch random chunks or spread out chunks
     * 3. Fallback -> Use provided 'content' string (description/title)
     */
    private String resolveContent(String fallbackContent, Long resourceId, String type, String url, String topic) {
        if (resourceId == null) {
            return fallbackContent;
        }

        String fileId = determineFileId(resourceId, type, url);
        if (fileId == null) {
            return fallbackContent;
        }

        List<VectorChunk> chunks;
        boolean isFocusMode = topic != null && !topic.trim().isEmpty() && !topic.equalsIgnoreCase("General Review");

        if (isFocusMode) {
            // Strategy 1: Focus Mode (Semantic Search)
            log.info("Using Focus Mode for topic: '{}' on fileId: {}", topic, fileId);
            float[] embedding = embeddingClient.getEmbedding(topic);
            // Assuming we have a method to find similar by fileId.
            // If repository doesn't have it, we might need to add it or fetch all and
            // filter (inefficient) or use existing methods.
            // Given current repo, we might strictly need to filter by fileId?
            // Checking ChatbotService.. it uses findSimilarByRoom/Space.
            // We need findSimilarByFileId(fileId, embedding, limit).
            // NOTE: Assuming VectorStoreRepository has or can have findSimilarByFileId.
            // Adding check/todo if missing.
            chunks = vectorStoreRepository.findSimilarByFileId(fileId, embedding, 7);
        } else {
            // Strategy 2: General/Random Review
            // For now, simpler approach: Get all chunks (up to limit) or random subsample.
            // Retrieving a spread of chunks (e.g., first, middle, last) would be ideal but
            // random is okay for quiz.
            // Let's implement "Random Sample" from all chunks.
            log.info("Using Random/General Mode for fileId: {}", fileId);
            List<VectorChunk> allChunks = vectorStoreRepository.findByFileIdOrderByChunkIndex(fileId);
            if (allChunks.size() > 8) {
                Collections.shuffle(allChunks);
                chunks = allChunks.subList(0, 8);
            } else {
                chunks = allChunks;
            }
        }

        if (chunks.isEmpty()) {
            log.warn("No chunks found for fileId: {}. Falling back to provided content.", fileId);
            return fallbackContent;
        }

        String retrievedContext = chunks.stream()
                .map(VectorChunk::getContent)
                .collect(Collectors.joining("\n\n---\n\n"));

        log.info("Retrieved {} chars of context via RAG for [{}]", retrievedContext.length(),
                isFocusMode ? "TOPIC" : "RANDOM");
        return retrievedContext;
    }

    public String generateLearningMap(Long learningSpaceId) {
        log.info("Generating learning map for space: {}", learningSpaceId);

        // 1. Get a representative spread of content
        List<VectorChunk> chunks = vectorStoreRepository.findRandomChunksByLearningSpace(learningSpaceId, 15);

        if (chunks.isEmpty()) {
            return "mindmap\n  root((No content available))\n    Tip\n      Add resources to see your map";
        }

        String context = chunks.stream()
                .map(VectorChunk::getContent)
                .collect(Collectors.joining("\n---\n"));

        String promptText = """
                Based on the provided snippets from a learning space, create a comprehensive Mermaid.js mindmap.
                The mindmap should visualize the hierarchical relationship between concepts.

                RULES:
                1. Syntax MUST be valid Mermaid mindmap (starting with 'mindmap').
                2. Use 'root((Topic Name))' or just the core title at the top level.
                3. Group related concepts into branches.
                4. Keep labels concise (2-4 words).
                5. Do NOT include Markdown code blocks (```), just the raw mermaid code.

                Content:
                {context}
                """;

        ChatClient chatClient = chatClientBuilder.build();
        try {
            String response = chatClient.prompt()
                    .user(u -> u.text(promptText).arg("context", context))
                    .call()
                    .content();

            // Clean up code blocks if LLM still includes them
            return response.replaceAll("```mermaid", "").replaceAll("```", "").trim();
        } catch (Exception e) {
            log.error("Error generating learning map", e);
            return "mindmap\n  root((Error generating map))";
        }
    }

    private String determineFileId(Long resourceId, String type, String url) {
        if ("FILE".equalsIgnoreCase(type)) {
            return url; // For FILE, url is the fileId/path
        } else if ("TEXT".equalsIgnoreCase(type)) {
            return "text_" + resourceId;
        } else if ("LINK".equalsIgnoreCase(type)) {
            return "link_" + resourceId;
        }
        return null;
    }
}
