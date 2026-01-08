package com.studyhub.ai_service.service;

import com.studyhub.ai_service.entity.VectorChunk;
import com.studyhub.ai_service.repository.VectorStoreRepository;
import com.studyhub.ai_service.client.GeminiEmbeddingClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final ChatModel chatModel;
    private final GeminiEmbeddingClient embeddingClient;
    private final VectorStoreRepository vectorStoreRepository;

    private static final String SYSTEM_PROMPT = """
            You are a helpful study assistant for StudyHub.
            Use the provided context from document segments to answer the user's question accurately.
            If the answer is not in the context, say that you don't know based on the documents, but try to be helpful.

            Context:
            {context}
            """;

    public String ask(Long roomId, String userQuery) {
        log.info("Answering query for room {}: {}", roomId, userQuery);

        // 1. Generate query embedding
        List<Double> queryEmbedding = embeddingClient.getEmbedding(userQuery);

        // 2. Retrieve top-k relevant fragments
        List<VectorChunk> contextChunks = vectorStoreRepository.findSimilarByRoom(roomId, queryEmbedding, 5);

        String context = contextChunks.stream()
                .map(VectorChunk::getContent)
                .collect(Collectors.joining("\n\n---\n\n"));

        log.debug("Found {} relevant chunks for dynamic context", contextChunks.size());

        // 3. Create prompt
        PromptTemplate promptTemplate = new PromptTemplate(SYSTEM_PROMPT);
        var systemMessage = new SystemMessage(promptTemplate.render(Map.of("context", context)));
        var userMessage = new UserMessage(userQuery);

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        // 4. Call LLM
        return chatModel.call(prompt).getResult().getOutput().getContent();
    }
}
