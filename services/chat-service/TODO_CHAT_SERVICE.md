# Chat Service Implementation TODOs

This document lists prioritized tasks to finish the chat service with concise implementation steps.

---

## 1. Add User Feign Client
- **Mục tiêu:** Gọi `user-service` để lấy thông tin người dùng (username, fullName, avatarUrl).
- **Tại sao cần:** Thay placeholder `user` info bằng dữ liệu thực.
- **Các bước:**
  1. Add `spring-cloud-starter-openfeign` dependency to `pom.xml` of chat-service.
  2. Annotate main class or config with `@EnableFeignClients`.
  3. Create a new Feign client interface `UserClient` in `com.studyhub.chat_service.client` with method e.g. `@GetMapping("/api/v1/users/{id}") UserResponse getUserById(@PathVariable Long id);`.
  4. Create `dto` mapping `UserResponse` to the one in `user-service` if required (keep DTO fields minimal for `username`, `firstName`, `lastName`, `profilePictureUrl`).
  5. Use `UserClient` in `MessageService::toMessageResponse()` and `RoomService::toMemberResponse()` to fill `sender` and `member` details.
  6. Add retries/timeouts & failover (fallback) in Feign to avoid blocking WebSocket flow if user-service is down.
- **Files to edit:**
  - `pom.xml` (add feign dependency)
  - `ChatServiceApplication.java` (or `FeignConfig`) to enable Feign
  - `src/main/java/com/studyhub/chat_service/client/UserClient.java`
  - `MessageService.java`, `RoomService.java` to use client
- **Tests:** Unit tests mocking Feign client, integration tests for Feign calls.

---

## 2. Replace `MOCK_USER_ID` with JWT Principal
- **Mục tiêu:** Use authenticated user from JWT instead of `MOCK_USER_ID` to support real users.
- **Tại sao cần:** Security and correct attribution of messages.
- **Các bước (REST):**
  1. In REST controllers (`RoomController`, `MessageController`), inject the `@AuthenticationPrincipal Jwt jwt` or `@AuthenticationPrincipal(expression = "sub") String keycloakUserId` or resolved user principal.
  2. If chat-service tracks internal user ID separate from Keycloak subject, fetch the internal `Long userId` from `UserService` via Feign (map keycloak subject or other identifier to internal user ID).
  3. Replace `MOCK_USER_ID` with retrieved `userId`.
- **Các bước (WebSocket):**
  1. Implement a handshake interceptor that extracts `Authorization: Bearer <token>` from the STOMP headers (during handshake).
  2. Validate or parse the JWT using Spring Security's JwtDecoder or set the `Principal` for the session.
  3. In the controller `ChatWebSocketController`, use `Principal` or `@Header("simpUser")` to find the user's id/subject and map to internal user id via Feign client if needed.
- **Files to edit:**
  - `ChatWebSocketController.java` (replace `MOCK_USER_ID` usage and accept `Principal`/`Jwt`)
  - `MessageController.java`, `RoomController.java` (use `@AuthenticationPrincipal Jwt jwt`)
  - Add `JwtHandshakeInterceptor` (or `StompPrincipal`) in WebSocket config.
- **Tests:** Unit tests for controller with mock `Principal/Jwt`, e2e WebSocket tests using SockJS/STOMP with Authorization header.

---

## 3. Secure WebSocket & REST Endpoints
- **Mục tiêu:** Ensure only authenticated users can call restricted endpoints and subscribe to rooms they are members of.
- **Các bước:**
  1. Update `SecurityConfig` to protect REST endpoints: require JWT for `/api/**` and define scopes/roles if needed.
  2. Protect WebSocket handshake: ensure handshake uses a valid Bearer token.
  3. In message handlers and room APIs validate membership using `roomRepository.existsMemberInRoom(...)` before allowing any action.
  4. Add rate limiting if desired (per user per second to deter spam).
- **Files to edit:**
  - `SecurityConfig.java` (adjust resource-server settings)
  - `WebSocketConfig.java`, add handshake interceptor
  - `ChatWebSocketController.java` and REST controllers to verify membership.
- **Tests:** Security integration tests using a test JWT, verifying authorization works.

---

## 4. Populate Sender/Member Details from User Service
- **Mục tiêu:** Replace placeholder username/fullName with real user data in messages and members responses.
- **Các bước:**
  1. With `UserClient` in place, fetch user info in the service helper methods:
     - `MessageService::toMessageResponse()` — call `UserClient.getUserById(senderId)`.
     - `RoomService::toMemberResponse()` — call `UserClient.getUserById(memberId)`.
  2. Cache frequently used user info in memory or Redis for performance; set TTL.
  3. Handle fallback to placeholder if user-service call fails.
- **Files to edit:**
  - `MessageService.java`, `RoomService.java`.
  - Optional: new `UserCache` or `UserServiceProxy`.
- **Tests:** Unit test for mapping, integration test verifying end-to-end data.

---

## 5. Publish Message Events to Kafka / Indexing for Search
- **Mục tiêu:** Emit real-time events for other services (search, analytics, notifications), index messages for full-text search.
- **Các bước:**
  1. Add dependency `spring-kafka` to `pom.xml` and configure `KafkaTemplate` properties (bootstrap servers, producer settings).
  2. Define a topic `chat.messages` and message event DTO for `MessageCreated`, `MessageEdited`, `MessageDeleted`.
  3. In `MessageService` methods, after persisting, create and send an event to the topic using `KafkaTemplate`. Include message details and user info.
  4. Create a consumer (or rely on `search-service` to consume and index messages).
- **Files to edit:**
  - `pom.xml` (add kafka)
  - `MessageService.java` (produce events)
  - `application.yml` (kafka config) in `ops` or `chat-service` resources.
- **Tests:** Integration Kafka test(s) using `EmbeddedKafka`.

---

## 6. Add Tests & Update API docs
- **Mục tiêu:** Ensure reliability & maintainability.
- **Các bước:**
  1. Add unit tests for `MessageService` and `RoomService` mocking repositories and Feign clients.
  2. Add controller tests for REST endpoints with `@WebMvcTest` and mock security principal.
  3. Add integration tests for STOMP with a test WebSocket client and a test Jwt or a preconfigured dev security profile.
  4. Ensure OpenAPI documentation includes new endpoints or updated schemas and refresh Postman collection.
- **Files to edit:**
  - under `src/test/java/...` add new tests.
  - `pom.xml` may need test dependencies (JUnit, Mockito, Spring boot test, Embedded Kafka.)
  - Update `Chat-Service-CRUD.postman_collection.json` or include new requests.

---

### Prioritization
1. **Add User Feign Client** — immediate improvement; low risk.
2. **Replace MOCK_USER_ID with JWT Principal** — crucial for auth and correctness.
3. **Populate sender/member details** — requires (1) and (2) to be done.
4. **Secure WebSocket & REST** — ensure production readiness.
5. **Publish events to Kafka** — integrate with other services.
6. **Testing & docs** — continuous.

---

### Notes
- Keep changes minimal and backward compatible; add feature flags or `dev` profiles to ease local development.
- If `user-service` does not expose a simple `GET /api/v1/users/{id}` endpoint, either add it there or use `GET /api/v1/users/me` via JWT-subject to map Keycloak subject to internal id.
- Consider caching user info to reduce Feign calls inside high-throughput flows.

---

If you want, I can now start implementing task 1 (Add User Feign Client) and task 4 (Populate sender/member details) next. Let me know to proceed.