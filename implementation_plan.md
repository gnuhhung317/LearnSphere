# Socratic Notes Implementation Plan

## Goal Description
Implement "Socratic Notes" in the Learning Space. This transforms the AI chat into a "Cognitive Assistant" using the Socratic method to guide users to deep understanding before synthesizing notes. It includes specific tools (Analogy, Challenge), a Synthesis mode for Notion-ready notes, and client-side session management.

## User Review Required
> [!IMPORTANT]
> **Stateless Backend Chat**: The current implementation of `askLearningSpace` is stateless. To support the Socratic method (which depends on conversation context), we will update the backend to accept `chatHistory` from the client. This aligns with the "Local Storage" requirement where the client manages the session state.

## Proposed Changes

### Backend: `ai-service`

#### [MODIFY] [AiChatController.java](file:///d:/Code/Projects/learning-project/studyhub/services/ai-service/src/main/java/com/studyhub/ai_service/controller/AiChatController.java)
- Update `chatLearningSpace` to accept `history` (List of messages) and `mode` (optional) in the request body.

#### [MODIFY] [ChatbotService.java](file:///d:/Code/Projects/learning-project/studyhub/services/ai-service/src/main/java/com/studyhub/ai_service/service/ChatbotService.java)
- Update `askLearningSpace` to accept `List<MessageDto>` (or similar) for history.
- Define `SOCRATIC_SYSTEM_PROMPT` following the "Core Dialogue Logic".
- Implement logic to switch between standard/socratic prompts or use a single comprehensive prompt.
- Add `channelId` handling if we want to support persistent group chats later, but for now focus on the stateless `learningSpace` flow.

### Frontend: `studyhub-frontend`

#### [MODIFY] [ai.service.ts](file:///d:/Code/Projects/learning-project/studyhub/frontend/lib/services/ai.service.ts)
- Update `AIChatRequest` interface to explicitly include `mode` ('SOCRATIC' | 'STANDARD').

#### [MODIFY] [ai-chat-panel.tsx](file:///d:/Code/Projects/learning-project/studyhub/frontend/features/learning/components/ai-chat-panel.tsx)
- **UI Overhaul**:
    - Add "Context Tools" buttons (Analogy, Challenge) above/near input.
    - Implement "Sidebar" for Session History (using `localStorage`).
    - Add "Synthesize" button or handle keyword triggers.
- **Logic**:
    - Persist `messages` to `localStorage`.
    - Handle session switching (New Chat, Delete Chat).
    - Send `history` to backend.

#### [NEW] [socratic-session-manager.ts](file:///d:/Code/Projects/learning-project/studyhub/frontend/lib/services/socratic-session-manager.ts)
- Helper class/service to handle LocalStorage CRUD for chat sessions.

## Verification Plan

### Automated Tests
- Since I cannot run full integration tests easily without the whole stack up, I will rely on manual verification and code consistency checks.
- I will attempt to run `mvn test` if applicable for the service.

### Manual Verification
1.  **Socratic Flow**:
    - Open Learning Space -> Open AI Chat.
    - User types a topic (e.g., "Docker").
    - Verify AI asks a diagnostic question instead of explaining.
2.  **Tools**:
    - Click "Analogy Generator". Verify AI gives an ELI5 analogy.
    - Click "Challenge Me". Verify AI critiques the idea.
3.  **Synthesis**:
    - User types "Synthesize".
    - Verify AI generates a Notion-formatted note (Markdown, Mermaid, Tags).
4.  **Session Management**:
    - Refresh page. Verify chat history persists.
    - Create "New Note". Verify history clears (visually) and saves to sidebar.
    - Switch between sessions.
