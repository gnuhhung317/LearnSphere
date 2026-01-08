package com.studyhub.ai_service.service.etl;

import com.studyhub.ai_service.client.GeminiEmbeddingClient;
import com.studyhub.ai_service.entity.VectorChunk;
import com.studyhub.ai_service.repository.VectorStoreRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingQueueService {

    private final GeminiEmbeddingClient embeddingClient;
    private final VectorStoreRepository vectorStoreRepository;

    // In-memory queue for simplified rate limiting
    private final Queue<ChunkTask> queue = new ConcurrentLinkedQueue<>();

    @Data
    @Builder
    public static class ChunkTask {
        private String fileId;
        private Long roomId;
        private String content;
        private int chunkIndex;
        private Runnable onSuccess;
    }

    public void enqueue(String fileId, Long roomId, String content, int index, Runnable onSuccess) {
        queue.offer(ChunkTask.builder()
                .fileId(fileId)
                .roomId(roomId)
                .content(content)
                .chunkIndex(index)
                .onSuccess(onSuccess)
                .build());
    }

    /**
     * Process queue with rate limiting.
     * Run every 2000ms (2 seconds) to stay well within typical free tier limits
     * (e.g. 60 RPM).
     * Adjust 'fixedDelay' as needed.
     */
    @Scheduled(fixedDelay = 2000)
    public void processNextChunk() {
        if (queue.isEmpty()) {
            return;
        }

        ChunkTask task = queue.poll();
        if (task == null)
            return;

        try {
            log.debug("Embedding chunk {} for file {}", task.getChunkIndex(), task.getFileId());

            // 1. Get Embedding
            List<Double> embedding = embeddingClient.getEmbedding(task.getContent());

            // 2. Save to DB
            VectorChunk chunk = VectorChunk.builder()
                    .fileId(task.getFileId())
                    .content(task.getContent())
                    .chunkIndex(task.getChunkIndex())
                    .build();

            vectorStoreRepository.save(chunk, embedding, task.getRoomId());

            // 3. Callback (e.g. to check if file processing is complete)
            if (task.getOnSuccess() != null) {
                task.getOnSuccess().run();
            }

        } catch (Exception e) {
            log.error("Failed to process chunk {} for file {}", task.getChunkIndex(), task.getFileId(), e);
            // Retry logic could be added here (e.g. re-enqueue with attempt count)
        }
    }
}
