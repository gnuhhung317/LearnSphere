package com.studyhub.ai_service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class GeminiEmbeddingClient {

    private final EmbeddingModel embeddingModel;

    public List<Double> getEmbedding(String text) {
        log.info("Generating embedding for text length: {}", text.length());
        return embeddingModel.embed(text);
    }
}
