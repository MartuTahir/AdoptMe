# Design: Chat-Implementation

## Technical Approach

Implement a secure, paginated chat system between Adopters and Shelters using Hexagonal Architecture. Communication is only permitted for `AdoptionRequest` in `ACCEPTED` status. Messages will be stored in Neo4j linked to the `AdoptionRequest` node.

## Architecture Decisions

### Decision: Message Persistence Model

**Choice**: Link `Message` nodes to `AdoptionRequest` via `[:CONTAINS]` relationship.
**Alternatives considered**: Separate `Chat` node, or linking messages directly to `User` and `Pet`.
**Rationale**: The business rule specifies that the chat is contextually tied to an adoption process. Linking to `AdoptionRequest` simplifies authorization (one path to check state and ownership).

### Decision: Authorization Layer

**Choice**: Application Service (`ChatService`) responsibility.
**Alternatives considered**: Spring Security `@PreAuthorize` with custom SpEL, or Database-level constraints.
**Rationale**: Checking if a user is part of a specific `AdoptionRequest` requires complex logic (checking if user is the requester OR the owner of the pet). Placing this in the service layer ensures consistency across different entry points (Web, and potentially future WebSockets).

### Decision: Pagination Strategy

**Choice**: Offset-based pagination (`SKIP $skip LIMIT $limit`) ordered by `timestamp DESC`.
**Alternatives considered**: Cursor-based pagination.
**Rationale**: Given the expected volume of messages per adoption request, offset-based is sufficient and easier to implement with standard Spring Data REST patterns.

## Data Flow

1.  **SendMessage**: `ChatController` -> `SendMessageUseCase` -> `ChatService` -> `ChatPersistencePort` -> `Neo4jMessageAdapter` -> `Neo4jClient`.
2.  **GetHistory**: `ChatController` -> `GetChatHistoryUseCase` -> `ChatService` -> `ChatPersistencePort` -> `Neo4jMessageAdapter` -> `Neo4jClient`.

```
  User A (Adopter) ────→ [ChatController] ──→ [ChatService] ──→ [Neo4j]
                             │                    │
                             └─ Auth Check ───────┘
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `src/main/java/com/matchpet/domain/model/Message.java` | Create | Domain Record for chat messages. |
| `src/main/java/com/matchpet/application/ports/input/SendMessageUseCase.java` | Create | Input port for sending messages. |
| `src/main/java/com/matchpet/application/ports/input/GetChatHistoryUseCase.java` | Create | Input port for retrieving history. |
| `src/main/java/com/matchpet/application/ports/output/ChatPersistencePort.java` | Create | Output port for Neo4j persistence. |
| `src/main/java/com/matchpet/application/services/ChatService.java` | Create | Implementation of chat use cases with validations. |
| `src/main/java/com/matchpet/infrastructure/adapters/output/neo4j/Neo4jMessageAdapter.java` | Create | Neo4j implementation using Cypher. |
| `src/main/java/com/matchpet/infrastructure/adapters/input/web/ChatController.java` | Create | REST API for chat. |
| `src/main/java/com/matchpet/infrastructure/adapters/input/web/config/SecurityConfig.java` | Modify | Permit chat endpoints for authenticated users. |

## Interfaces / Contracts

### Domain Model
```java
public record Message(
    String id,
    String senderId,
    String content,
    Instant timestamp
) {}
```

### API Endpoints
- `POST /api/chats/{requestId}/messages`
  - Body: `{ "content": "string" }`
  - Returns: `201 Created` + `MessageResponse`
- `GET /api/chats/{requestId}/messages?page=0&size=20`
  - Returns: `200 OK` + `Page<MessageResponse>`

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Unit | `ChatService` validations | Mock ports, verify `AuthorizationException` and `IllegalStateException`. |
| Integration | `Neo4jMessageAdapter` | Testcontainers (Neo4j), verify Cypher queries and mapping. |
| E2E | `ChatController` | `MockMvc`, full flow with JWT authentication and real DB. |

## Migration / Rollout

No migration required. New nodes and relationships will be created as users start chatting.

## Open Questions

- [ ] Should we limit the history size or archiving messages after adoption completion? (Deferred for now)
- [ ] Do we need real-time notifications (SSE/WebSockets)? (Out of scope for this change)
