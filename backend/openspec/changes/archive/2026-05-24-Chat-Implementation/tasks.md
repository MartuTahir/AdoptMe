# Tasks: Chat-Implementation

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | 250-350 |
| 400-line budget risk | Low |
| Chained PRs recommended | No |
| Suggested split | Single PR |
| Delivery strategy | ask-on-risk |
| Chain strategy | N/A |

Decision needed before apply: No
Chained PRs recommended: No
Chain strategy: N/A
400-line budget risk: Low

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | Complete chat implementation with tests | PR 1 | Single reviewable unit; tests/docs included |

## Phase 1: Foundation (Domain & Ports)

- [x] 1.1 Create `src/main/java/com/matchpet/domain/model/Message.java` record (id, senderId, content, timestamp)
- [x] 1.2 Create `src/main/java/com/matchpet/application/ports/input/SendMessageUseCase.java` interface
- [x] 1.3 Create `src/main/java/com/matchpet/application/ports/input/GetChatHistoryUseCase.java` interface
- [x] 1.4 Create `src/main/java/com/matchpet/application/ports/output/ChatPersistencePort.java` interface

## Phase 2: Core Implementation (Service & Persistence)

- [x] 2.1 Create `src/main/java/com/matchpet/application/services/ChatService.java` implementing both input ports with authorization logic (verify requestId ACCEPTED status, verify userId is participant)
- [x] 2.2 Add validation logic: content max 1000 chars, request exists, state is ACCEPTED, user is adopter or shelter owner
- [x] 2.3 Create `src/main/java/com/matchpet/infrastructure/adapters/output/neo4j/Neo4jMessageAdapter.java` implementing ChatPersistencePort
- [x] 2.4 Implement Cypher for saveMessage: `MATCH (ar:AdoptionRequest {id: $requestId}) CREATE (ar)-[:CONTAINS]->(m:Message {id: $id, senderId: $senderId, content: $content, timestamp: $timestamp})`
- [x] 2.5 Implement Cypher for getChatHistory with pagination: `MATCH (ar:AdoptionRequest {id: $requestId})-[:CONTAINS]->(m:Message) RETURN m ORDER BY m.timestamp DESC SKIP $skip LIMIT $limit`

## Phase 3: API Integration (Controller & Security)

- [x] 3.1 Create `src/main/java/com/matchpet/infrastructure/adapters/input/web/ChatController.java` with endpoints POST /api/chats/{requestId}/messages and GET /api/chats/{requestId}/messages
- [x] 3.2 Add DTOs: `SendMessageRequest` (content), `MessageResponse` (id, senderId, content, timestamp)
- [x] 3.3 Modify `src/main/java/com/matchpet/infrastructure/adapters/input/web/config/SecurityConfig.java` to permit `/api/chats/**` for authenticated users
- [x] 3.4 Wire ChatService bean in Spring configuration with required dependencies

## Phase 4: Testing

- [x] 4.1 Unit test ChatService: verify AuthorizationException when user not participant (spec: chat-authorization.feature scenarios)
- [x] 4.2 Unit test ChatService: verify IllegalStateException when request state not ACCEPTED (spec: chat-request-state.feature scenarios)
- [x] 4.3 Unit test ChatService: verify ValidationException when content > 1000 chars (spec: chat-send-message.feature)
- [x] 4.4 Integration test Neo4jMessageAdapter with Testcontainers: verify Cypher queries return correct data and pagination works (spec: chat-history.feature)
- [x] 4.5 E2E test ChatController with MockMvc: POST message returns 201, GET history returns 200 with paginated data (spec: all scenarios)
- [x] 4.6 E2E test authorization: verify 403 for non-participants, 404 for non-existent requestId (spec: chat-authorization.feature)

## Phase 5: Documentation

- [x] 5.1 Add JavaDoc to Message record, use cases, and service class
- [x] 5.2 Update project architecture diagram if exists to show chat module integration
