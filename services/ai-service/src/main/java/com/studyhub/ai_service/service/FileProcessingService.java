package com.studyhub.ai_service.service;

import com.studyhub.ai_service.client.ChatServiceClient;
import com.studyhub.ai_service.event.FileSyncedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileProcessingService {

    private final ChatServiceClient chatServiceClient;
    private final com.studyhub.ai_service.client.MediaServiceClient mediaService;
    private final com.studyhub.ai_service.service.etl.TikaExtractService tikaExtractService;
    private final com.studyhub.ai_service.service.etl.TextChunker textChunker;
    private final com.studyhub.ai_service.service.etl.EmbeddingQueueService embeddingQueueService;

    public void processFile(FileSyncedEvent event) {
        log.info("Processing file: {} (Attachment ID: {})", event.getFileId(), event.getAttachmentId());

        try {
            // 1. Update status to PROCESSING
            updateStatus(event.getAttachmentId(), "PROCESSING");

            // 2. Download file
            feign.Response response = mediaService.downloadFile(event.getFileId());
            if (response.status() != 200) {
                throw new RuntimeException("Failed to download file: " + response.status());
            }

            // 3. Extract Text via Tika
            String text;
            try (java.io.InputStream inputStream = response.body().asInputStream()) {
                text = tikaExtractService.extractText(inputStream);
            }
            log.info("Extracted {} chars from file {}", text.length(), event.getFileId());

            // 4. Chunk Text
            List<String> chunks = textChunker.chunk(text);
            log.info("Split file {} into {} chunks", event.getFileId(), chunks.size());

            if (chunks.isEmpty()) {
                updateStatus(event.getAttachmentId(), "READY"); // Nothing to embed
                return;
            }

            // 5. Enqueue Chunks
            for (int i = 0; i < chunks.size(); i++) {
                String chunkContent = chunks.get(i);
                boolean isLast = (i == chunks.size() - 1);

                Runnable callback = null;
                if (isLast) {
                    callback = () -> {
                        log.info("Finished processing file {}", event.getFileId());
                        updateStatus(event.getAttachmentId(), "READY");
                    };
                }

                embeddingQueueService.enqueue(
                        event.getFileId(),
                        event.getRoomId(),
                        chunkContent,
                        i,
                        callback);
            }

        } catch (Exception e) {
            log.error("Error processing file", e);
            updateStatus(event.getAttachmentId(), "FAILED");
        }
    }

    private void updateStatus(Long attachmentId, String status) {
        try {
            chatServiceClient.updateFileStatus(attachmentId, status);
        } catch (Exception e) {
            // Retry?
            log.error("Failed to update status for attachment {}", attachmentId, e);
        }
    }
}
