package com.studyhub.ai_service.service;

import com.studyhub.ai_service.entity.AIMessage;
import com.studyhub.ai_service.entity.AISession;
import com.studyhub.ai_service.entity.VectorChunk;
import com.studyhub.ai_service.repository.AIMessageRepository;
import com.studyhub.ai_service.repository.AISessionRepository;
import com.studyhub.ai_service.repository.VectorStoreRepository;
import com.studyhub.ai_service.client.GeminiEmbeddingClient;
import com.studyhub.ai_service.client.ChatServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
        private final ChatServiceClient chatServiceClient;
        private final AISessionRepository sessionRepository;
        private final AIMessageRepository messageRepository;

        private static final String RAG_SYSTEM_PROMPT = """
                        You are a helpful study assistant for StudyHub.
                        Use the provided context from document segments to answer the user's question accurately.
                        If the answer is not in the context, say that you don't know based on the documents, but try to be helpful.

                        Context from documents:
                        {context}
                        """;

        private static final String SOCRATIC_SYSTEM_PROMPT = """
                        You are a "Socratic Tutor" for StudyHub.
                        Your goal is NOT to give direct answers, but to guide the user to understanding through questioning.

                        RULES:
                        1. Active Learning: Ask diagnostic questions to check current understanding.
                        2. Scaffolding: Provide hints, not solutions. Connect new concepts to known ones.
                        3. Context: Use the provided document context to form your questions and verify user answers.
                        4. Interactive Quizzes: Frequently use interactive quizzes to test concepts.
                           Format: Use a markdown code block with 'quiz' language and a JSON body.
                           Example:
                           ```quiz
                           {
                             "question": "What is the capital of France?",
                             "options": ["Paris", "London", "Berlin", "Madrid"],
                             "correctAnswer": "Paris",
                             "explanation": "Paris is the historical and current capital of France."
                           }
                           ```
                        5. Length: Keep responses concise and conversational.

                        Context from documents:
                        {context}
                        """;

        private static final String NOTE_ARCHITECT_PROMPT = """
                        You are a "Note Architect". The user wants to synthesize the discussion into a structured note.

                        RULES:
                        1. Output format: Markdown (Notion-friendly).
                        2. Structure:
                           - Context/Why (Callout block)
                           - Key Concepts (H2, H3)
                           - Comparison Tables (if applicable)
                           - Mermaid Diagrams (if process/flow)
                        3. Tags: Add #tags at the end.

                        Context from documents:
                        {context}
                        """;

        @Transactional(readOnly = true)
        public List<AISession> getSessions(Long userId, Long learningSpaceId) {
                return sessionRepository.findAllByUserIdAndLearningSpaceIdOrderByUpdatedAtDesc(userId, learningSpaceId);
        }

        @Transactional
        public AISession createSession(Long userId, Long learningSpaceId, String mode) {
                AISession session = AISession.builder()
                                .userId(userId)
                                .learningSpaceId(learningSpaceId)
                                .mode(mode)
                                .title("New Chat")
                                .build();
                return sessionRepository.save(session);
        }

        @Transactional
        public void deleteSession(Long sessionId, Long userId) {
                AISession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                                .orElseThrow(() -> new RuntimeException("Session not found or access denied"));
                // Cascade delete messages if needed, usually handled by DB, but safe to delete
                // manually if logic requires
                // Assuming JPA cascade or manual deletion:
                List<AIMessage> messages = messageRepository.findAllBySessionIdOrderByCreatedAtAsc(sessionId);
                messageRepository.deleteAll(messages);
                sessionRepository.delete(session);
        }

        @Transactional(readOnly = true)
        public List<AIMessage> getSessionMessages(Long sessionId) {
                return messageRepository.findAllBySessionIdOrderByCreatedAtAsc(sessionId);
        }

        @Transactional
        public String askSession(Long sessionId, String userQuery) {
                AISession session = sessionRepository.findById(sessionId)
                                .orElseThrow(() -> new RuntimeException("Session not found"));

                log.info("Answering query for session {}: {}", sessionId, userQuery);

                // 1. Load History
                List<AIMessage> history = messageRepository.findAllBySessionIdOrderByCreatedAtAsc(sessionId);
                List<Message> messageHistory = history.stream()
                                .map(msg -> msg.getRole() == AIMessage.Role.USER ? new UserMessage(msg.getContent())
                                                : new org.springframework.ai.chat.messages.AssistantMessage(
                                                                msg.getContent()))
                                .collect(Collectors.toList());

                // 2. Resolve Context
                float[] queryEmbedding = embeddingClient.getEmbedding(userQuery);
                List<VectorChunk> contextChunks = vectorStoreRepository
                                .findSimilarByLearningSpace(session.getLearningSpaceId(), queryEmbedding, 5);
                String context = contextChunks.stream()
                                .map(VectorChunk::getContent)
                                .collect(Collectors.joining("\n\n---\n\n"));

                // 3. Select Prompt & Logic
                String systemPrompt = "RAG".equalsIgnoreCase(session.getMode()) ? RAG_SYSTEM_PROMPT
                                : SOCRATIC_SYSTEM_PROMPT;

                // Check for Synthesis Override
                if (userQuery.toLowerCase().contains("synthesize") || userQuery.toLowerCase().contains("create note")) {
                        systemPrompt = NOTE_ARCHITECT_PROMPT;
                }

                PromptTemplate promptTemplate = new PromptTemplate(systemPrompt);
                var systemMessage = new SystemMessage(promptTemplate.render(Map.of("context", context)));

                List<Message> promptMessages = new ArrayList<>();
                promptMessages.add(systemMessage);
                promptMessages.addAll(messageHistory); // Add history
                promptMessages.add(new UserMessage(userQuery)); // Add new query

                Prompt prompt = new Prompt(promptMessages);

                // 4. Call LLM
                String aiResponse = chatModel.call(prompt).getResult().getOutput().getText();

                // 5. Persist Messages
                AIMessage userMsg = AIMessage.builder().session(session).role(AIMessage.Role.USER).content(userQuery)
                                .build();
                AIMessage aiMsg = AIMessage.builder().session(session).role(AIMessage.Role.ASSISTANT)
                                .content(aiResponse).build();

                messageRepository.save(userMsg);
                messageRepository.save(aiMsg);

                // Auto-Title on first message
                if (history.isEmpty()) {
                        generateTitle(session, userQuery);
                }

                return aiResponse;
        }

        private void generateTitle(AISession session, String firstMessage) {
                try {
                        String prompt = "Generate a short (max 5 words) title for a chat starting with: "
                                        + firstMessage;
                        String title = chatModel.call(new Prompt(prompt)).getResult().getOutput().getText().trim()
                                        .replace("\"", "");
                        session.setTitle(title);
                        sessionRepository.save(session);
                } catch (Exception e) {
                        log.error("Failed to generate title", e);
                }
        }

        // Deprecated or Stateless methods
        public String ask(Long roomId, Long channelId, String userQuery) {
                // ... (Keep existing implementation if strictly needed, or deprecate)
                log.info("Answering query for room {}: {}", roomId, userQuery);
                float[] queryEmbedding = embeddingClient.getEmbedding(userQuery);
                List<VectorChunk> contextChunks = vectorStoreRepository.findSimilarByRoom(roomId, queryEmbedding, 5);
                String context = contextChunks.stream().map(VectorChunk::getContent)
                                .collect(Collectors.joining("\n\n---\n\n"));
                PromptTemplate promptTemplate = new PromptTemplate(RAG_SYSTEM_PROMPT);
                var systemMessage = new SystemMessage(promptTemplate.render(Map.of("context", context)));
                var userMessage = new UserMessage(userQuery);
                return chatModel.call(new Prompt(List.of(systemMessage, userMessage))).getResult().getOutput()
                                .getText();
        }

        public String askLearningSpace(Long learningSpaceId, String userQuery) {
                // ... (Keep existing implementation if strictly needed, or deprecate)
                return "Please use the Session-based API for full Socratic support.";
        }

        public String chatWithFile(String fileId, String message) {
                log.info("Chatting with file {}: {}", fileId, message);

                // 1. Resolve Context specific to the file
                float[] queryEmbedding = embeddingClient.getEmbedding(message);
                List<VectorChunk> contextChunks = vectorStoreRepository.findSimilarByFileId(fileId, queryEmbedding, 5);

                String context;
                if (contextChunks.isEmpty()) {
                        // Fallback: If no chunks found (maybe not indexed yet?), try to get all chunks
                        // if small, or warn.
                        List<VectorChunk> allChunks = vectorStoreRepository.findByFileIdOrderByChunkIndex(fileId);
                        if (allChunks.size() > 10) {
                                // Too large to put all in context without search. taking first few.
                                context = allChunks.subList(0, 5).stream()
                                                .map(VectorChunk::getContent)
                                                .collect(Collectors.joining("\n\n---\n\n"));
                        } else {
                                context = allChunks.stream()
                                                .map(VectorChunk::getContent)
                                                .collect(Collectors.joining("\n\n---\n\n"));
                        }
                } else {
                        context = contextChunks.stream()
                                        .map(VectorChunk::getContent)
                                        .collect(Collectors.joining("\n\n---\n\n"));
                }

                if (context.isEmpty()) {
                        context = "No content available for this file.";
                }

                // 2. Build Prompt
                String systemPrompt = """
                                You are a helpful assistant discussing a specific document.
                                Use the provided context from the document to answer the user's question.
                                If the answer is not in the context, say so.

                                Context:
                                {context}
                                """;

                PromptTemplate promptTemplate = new PromptTemplate(systemPrompt);
                var systemMessage = new SystemMessage(promptTemplate.render(Map.of("context", context)));
                var userMessage = new UserMessage(message);

                // 3. Call LLM
                return chatModel.call(new Prompt(List.of(systemMessage, userMessage))).getResult().getOutput()
                                .getText();
        }
}
