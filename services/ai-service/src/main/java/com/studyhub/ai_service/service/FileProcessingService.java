package com.studyhub.ai_service.service;

import com.studyhub.ai_service.client.ChatServiceClient;
import com.studyhub.ai_service.client.LearningServiceClient;
import com.studyhub.ai_service.client.MediaServiceClient;
import com.studyhub.ai_service.event.FileSyncedEvent;
import com.studyhub.ai_service.event.ResourceAddedEvent;
import com.studyhub.ai_service.service.etl.EmbeddingQueueService;
import com.studyhub.ai_service.service.etl.TextChunker;
import com.studyhub.ai_service.service.etl.TikaExtractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileProcessingService {

    private final ChatServiceClient chatServiceClient;
    private final MediaServiceClient mediaService;
    private final LearningServiceClient learningServiceClient;
    private final TikaExtractService tikaExtractService;
    private final TextChunker textChunker;
    private final EmbeddingQueueService embeddingQueueService;
    private final ChatModel chatModel;
    private final com.studyhub.ai_service.service.etl.WebExtractionService webExtractionService;
    private final com.studyhub.ai_service.repository.VectorStoreRepository vectorStoreRepository;
    private final LearningAssistantService learningAssistantService;

    public void processFile(FileSyncedEvent event) {
        log.info("Processing file: {} (Attachment ID: {})", event.getFileId(), event.getAttachmentId());
        try {
            // This might be wrong, attachmentId is not
            // resourceId.
            // Wait, FileSyncedEvent comes from Chat Service, resource creation comes from
            // Learning Service.
            // If it's a FileSyncedEvent, it's a chat attachment.
            // If it's a ResourceAddedEvent, it's a learning resource.
            // The user complaint is about Learning Resources.
            // I should focus on processResource method.

            updateStatus(event.getAttachmentId(), "PROCESSING");
            String text = extractTextFromFile(event.getFileId());
            processContent(text, event.getFileId(), event.getRoomId(), null);
            updateStatus(event.getAttachmentId(), "READY");
        } catch (Exception e) {
            log.error("Error processing file", e);
            updateStatus(event.getAttachmentId(), "FAILED");
        }
    }

    public void processResource(ResourceAddedEvent event) {
        log.info("Processing resource: {} (Type: {})", event.getResourceId(), event.getType());
        try {
            // 1. Set Status to PROCESSING
            learningServiceClient.updateResource(
                    event.getLearningSpaceId(),
                    event.getResourceId(),
                    LearningServiceClient.UpdateResourceRequest.builder()
                            .status("PROCESSING")
                            .build());

            String text = "";
            String fileId = null;

            if ("FILE".equalsIgnoreCase(event.getType())) {
                fileId = event.getUrl();
                text = extractTextFromFile(fileId);
            } else if ("TEXT".equalsIgnoreCase(event.getType())) {
                text = event.getUrl();
                fileId = "text_" + event.getResourceId();
            } else if ("LINK".equalsIgnoreCase(event.getType())) {
                text = webExtractionService.extractContent(event.getUrl());
                fileId = "link_" + event.getResourceId();
            }

            if (text != null && !text.isBlank()) {
                // 2. Embed content
                processContent(text, fileId, null, event.getLearningSpaceId());

                // 3. Generate Summary & Title
                String summary = generateSummary(text);
                String newTitle = null;
                if (event.getTitle() == null || event.getTitle().isBlank()
                        || "Untitled".equalsIgnoreCase(event.getTitle())) {
                    newTitle = generateTitle(text);
                }

                // 4. Update Resource with Summary, Title (if generated), and READY status
                learningServiceClient.updateResource(
                        event.getLearningSpaceId(),
                        event.getResourceId(),
                        LearningServiceClient.UpdateResourceRequest.builder()
                                .title(newTitle)
                                .description(summary)
                                .status("READY")
                                .build());

                // 5. Generate Flashcards
                try {
                    log.info("Generating auto-flashcards for resource {}", event.getResourceId());
                    com.studyhub.ai_service.dto.GeneratedFlashcardsResponse flashcards = learningAssistantService
                            .generateFlashcards(
                                    com.studyhub.ai_service.dto.GenerateFlashcardsRequest.builder()
                                            .content(text)
                                            .count(5)
                                            .topic("Key Concepts")
                                            .build());

                    if (flashcards != null && flashcards.getFlashcards() != null
                            && !flashcards.getFlashcards().isEmpty()) {
                        learningServiceClient.createFlashcardDeck(
                                event.getLearningSpaceId(),
                                event.getResourceId(),
                                LearningServiceClient.FlashcardDeckDto.builder()
                                        .title("Key Concepts: " + (newTitle != null ? newTitle : event.getTitle()))
                                        .flashcards(flashcards.getFlashcards().stream()
                                                .map(f -> LearningServiceClient.FlashcardDto.builder()
                                                        .front(f.getFront())
                                                        .back(f.getBack())
                                                        .build())
                                                .collect(java.util.stream.Collectors.toList()))
                                        .build());
                        log.info("Successfully generated and saved {} flashcards for resource {}",
                                flashcards.getFlashcards().size(), event.getResourceId());
                    }
                } catch (Exception e) {
                    log.error("Failed to generate auto-flashcards for resource {}", event.getResourceId(), e);
                }
            } else {
                // Empty content, just mark ready
                learningServiceClient.updateResource(
                        event.getLearningSpaceId(),
                        event.getResourceId(),
                        LearningServiceClient.UpdateResourceRequest.builder()
                                .status("READY")
                                .build());
            }

        } catch (Exception e) {
            log.error("Error processing resource {}", event.getResourceId(), e);
            try {
                learningServiceClient.updateResource(
                        event.getLearningSpaceId(),
                        event.getResourceId(),
                        LearningServiceClient.UpdateResourceRequest.builder()
                                .status("FAILED")
                                .build());
            } catch (Exception ex) {
                log.error("Failed to update resource status to FAILED", ex);
            }
        }
    }

    public void deleteResourceEmbeddings(com.studyhub.ai_service.event.ResourceDeletedEvent event) {
        log.info("Deleting embeddings for resource: {} (Type: {})", event.getResourceId(), event.getType());
        try {
            String fileId = null;
            if ("FILE".equalsIgnoreCase(event.getType())) {
                fileId = event.getUrl();
            } else if ("TEXT".equalsIgnoreCase(event.getType())) {
                fileId = "text_" + event.getResourceId();
            } else if ("LINK".equalsIgnoreCase(event.getType())) {
                fileId = "link_" + event.getResourceId();
            }

            if (fileId != null) {
                vectorStoreRepository.deleteByFileIdAndLearningSpaceId(fileId, event.getLearningSpaceId());
                log.info("Successfully deleted embeddings for fileId: {} in space: {}", fileId,
                        event.getLearningSpaceId());
            }
        } catch (Exception e) {
            log.error("Error deleting embeddings for resource {}", event.getResourceId(), e);
        }
    }

    public void deleteLearningSpaceEmbeddings(com.studyhub.ai_service.event.LearningSpaceDeletedEvent event) {
        log.info("Deleting all embeddings for learning space: {}", event.getLearningSpaceId());
        try {
            vectorStoreRepository.deleteByLearningSpaceId(event.getLearningSpaceId());
            log.info("Successfully deleted all embeddings for space: {}", event.getLearningSpaceId());
        } catch (Exception e) {
            log.error("Error deleting embeddings for space {}", event.getLearningSpaceId(), e);
        }
    }

    private String extractTextFromFile(String fileId) {
        feign.Response response = mediaService.downloadFile(fileId);
        if (response.status() != 200) {
            throw new RuntimeException("Failed to download file: " + response.status());
        }
        try (java.io.InputStream inputStream = response.body().asInputStream()) {
            String extracted = tikaExtractService.extractText(inputStream);
            log.info("Extracted {} chars from file {}", extracted.length(), fileId);
            return extracted;
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract text", e);
        }
    }

    private void processContent(String text, String fileId, Long roomId, Long learningSpaceId) {
        List<String> chunks = textChunker.chunk(text);
        log.info("Split content into {} chunks", chunks.size());

        for (int i = 0; i < chunks.size(); i++) {
            String chunkContent = chunks.get(i);
            embeddingQueueService.enqueue(
                    fileId,
                    roomId,
                    learningSpaceId,
                    chunkContent,
                    i,
                    null);
        }
    }

    private String generateSummary(String content) {
        try {
            // Truncate content if too long for summary context window
            String context = content.length() > 5000 ? content.substring(0, 5000) : content;
            String prompt = "Summarize the following content in 2-3 concise sentences:\n\n" + context;
            return chatModel.call(new Prompt(prompt)).getResult().getOutput().getText();
        } catch (Exception e) {
            log.error("Failed to generate summary", e);
            return "Summary generation failed.";
        }
    }

    private String generateTitle(String content) {
        try {
            String context = content.length() > 1000 ? content.substring(0, 1000) : content;
            String prompt = "Generate a very short, concise title (max 5-6 words) for the following content:\n\n"
                    + context;
            String title = chatModel.call(new Prompt(prompt)).getResult().getOutput().getText();
            return title.replaceAll("^\"|\"$", "").trim(); // Remove quotes if LLM adds them
        } catch (Exception e) {
            log.error("Failed to generate title", e);
            return "Untitled Resource";
        }
    }

    private void updateStatus(Long attachmentId, String status) {
        try {
            chatServiceClient.updateFileStatus(attachmentId, status);
        } catch (Exception e) {
            log.error("Failed to update status for attachment {}", attachmentId, e);
        }
    }
}
