# Tasks: Módulo 5 - Motor de Matches

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | 520–780 |
| 400-line budget risk | High |
| Chained PRs recommended | Yes |
| Suggested split | PR 1 → PR 2 → PR 3 |
| Delivery strategy | ask-on-risk |
| Chain strategy | pending |

Decision needed before apply: Yes
Chained PRs recommended: Yes
Chain strategy: pending
400-line budget risk: High

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | Domain + application services with unit tests, update SwipeService | PR 1 | Standalone; tests/docs included |
| 2 | Neo4j persistence adapters + integration tests | PR 2 | Base: PR 1 |
| 3 | Web controllers, security, error mapping + web tests | PR 3 | Base: PR 2 |

## Phase 1: Foundation

- [x] 1.1 Create `src/main/java/com/matchpet/domain/model/AdoptionRequest.java` and `AdoptionRequestStatus.java` with state rules (PENDING/ACCEPTED/REJECTED).
- [x] 1.2 Add `src/main/java/com/matchpet/domain/exception/AuthorizationException.java`.
- [x] 1.3 Add input ports `AcceptAdoptionRequestUseCase`, `RejectAdoptionRequestUseCase` and DTOs `AdoptionDecisionCommand`, `AdoptionRequestResult`.
- [x] 1.4 Add output port `AdoptionRequestPersistencePort`.
- [x] 1.5 Decide source of `shelterId` for authorization (auth principal vs request body) and reflect in command/DTO shape.

## Phase 2: Application Logic (TDD)

- [x] 2.1 RED: create `AdoptionRequestServiceTest` with scenarios: accept authorized, accept without ownership, accept already accepted, reject authorized, reject accepted (invalid state).
- [x] 2.2 GREEN: implement `AdoptionRequestService` with `@Transactional`, constructor injection, and port calls to pass tests.
- [x] 2.3 REFACTOR: clean service logic and map to `AdoptionRequestResult`.
- [x] 2.4 RED: update `SwipeServiceTest` to expect idempotent `createOrGetPending` on LIKE.
- [x] 2.5 GREEN: modify `SwipeService` to call `AdoptionRequestPersistencePort.createOrGetPending` on LIKE (keep DISLIKE unchanged).

## Phase 3: Persistence & Web Wiring (TDD)

- [x] 3.1 RED: add `Neo4jAdoptionRequestAdapterIntegrationTest` (pivoted to `Neo4jAdoptionRequestAdapterMockTest` due to Docker limits).
- [x] 3.2 GREEN: implement `Neo4jAdoptionRequestAdapter` + `AdoptionRequestNeo4jRepository` Cypher (verified with mocks).
- [x] 3.3 Update `PetNeo4jRepository` with ownership query and `SwipeNeo4jRepository` with LIKE existence query used by adapter.
- [x] 3.4 RED: add web tests for accept/reject endpoints with ROLE_REFUGIO and expected 403 on AuthorizationException.
- [x] 3.5 GREEN: create `AdoptionRequestController`, `AdoptionDecisionRequest` (Bean Validation), update `SecurityConfig` to restrict endpoints, and `GlobalExceptionHandler` mapping to 403.

## Phase 4: Verification

- [ ] 4.1 Ensure all spec scenarios are covered in unit/integration/web tests (accept/reject/idempotency/authorization).
- [ ] 4.2 Run `mvn test` and fix any regressions.
