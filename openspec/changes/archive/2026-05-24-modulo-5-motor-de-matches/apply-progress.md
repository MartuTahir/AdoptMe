## Implementation Progress

**Change**: modulo-5-motor-de-matches
**Mode**: Strict TDD

### Completed Tasks
- [x] 1.1 Create `src/main/java/com/matchpet/domain/model/AdoptionRequest.java` and `AdoptionRequestStatus.java` with state rules (PENDING/ACCEPTED/REJECTED).
- [x] 1.2 Add `src/main/java/com/matchpet/domain/exception/AuthorizationException.java`.
- [x] 1.3 Add input ports `AcceptAdoptionRequestUseCase`, `RejectAdoptionRequestUseCase` and DTOs `AdoptionDecisionCommand`, `AdoptionRequestResult`.
- [x] 1.4 Add output port `AdoptionRequestPersistencePort`.
- [x] 1.5 Decide source of `shelterId` for authorization (auth principal vs request body) and reflect in command/DTO shape.
- [x] 2.1 RED: create `AdoptionRequestServiceTest` with scenarios: accept authorized, accept without ownership, accept already accepted, reject authorized, reject accepted (invalid state).
- [x] 2.2 GREEN: implement `AdoptionRequestService` with `@Transactional`, constructor injection, and port calls to pass tests.
- [x] 2.3 REFACTOR: clean service logic and map to `AdoptionRequestResult`.
- [x] 2.4 RED: update `SwipeServiceTest` to expect idempotent `createOrGetPending` on LIKE.
- [x] 2.5 GREEN: modify `SwipeService` to call `AdoptionRequestPersistencePort.createOrGetPending` on LIKE (keep DISLIKE unchanged).

### Files Changed
| File | Action | What Was Done |
|------|--------|---------------|
| `src/main/java/com/matchpet/domain/model/AdoptionRequestStatus.java` | Created | Added adoption request states (PENDING/ACCEPTED/REJECTED). |
| `src/main/java/com/matchpet/domain/model/AdoptionRequest.java` | Created | Added domain entity with state transitions and composed id. |
| `src/test/java/com/matchpet/domain/model/AdoptionRequestTest.java` | Created | Added unit tests for default state and transitions. |
| `src/main/java/com/matchpet/domain/exception/AuthorizationException.java` | Created | Added authorization exception. |
| `src/main/java/com/matchpet/application/ports/input/AcceptAdoptionRequestUseCase.java` | Created | Added accept use case port. |
| `src/main/java/com/matchpet/application/ports/input/RejectAdoptionRequestUseCase.java` | Created | Added reject use case port. |
| `src/main/java/com/matchpet/application/ports/input/dto/AdoptionDecisionCommand.java` | Created | Added command DTO requiring requestId and shelterId. |
| `src/main/java/com/matchpet/application/ports/input/dto/AdoptionRequestResult.java` | Created | Added result DTO for adoption requests. |
| `src/main/java/com/matchpet/application/ports/output/AdoptionRequestPersistencePort.java` | Created | Added persistence port for adoption requests. |
| `src/main/java/com/matchpet/application/services/AdoptionRequestService.java` | Created | Added application service for accept/reject. |
| `src/test/java/com/matchpet/application/services/AdoptionRequestServiceTest.java` | Created | Added unit tests for accept/reject scenarios. |
| `src/main/java/com/matchpet/application/services/SwipeService.java` | Modified | Injected adoption request persistence and invoked createOrGetPending on LIKE. |
| `src/test/java/com/matchpet/application/services/SwipeServiceTest.java` | Modified | Added tests verifying adoption request creation on LIKE. |

### TDD Cycle Evidence
| Task | Test File | Layer | Safety Net | RED | GREEN | TRIANGULATE | REFACTOR |
|------|-----------|-------|------------|-----|-------|-------------|----------|
| 1.1 | `src/test/java/com/matchpet/domain/model/AdoptionRequestTest.java` | Unit | N/A (new) | ✅ Written | ✅ Passed (`mvn test -Dtest=AdoptionRequestTest`) | ✅ 5 cases | ✅ Clean |
| 2.1 | `src/test/java/com/matchpet/application/services/AdoptionRequestServiceTest.java` | Unit | N/A (new) | ✅ Written | ✅ Passed (`mvn test -Dtest=AdoptionRequestServiceTest`) | ✅ 5 cases | ✅ Clean |
| 2.4 | `src/test/java/com/matchpet/application/services/SwipeServiceTest.java` | Unit | ✅ 6/6 | ✅ Written | ✅ Passed (`mvn test -Dtest=SwipeServiceTest`) | ✅ 2 cases | ✅ Clean |

### Test Summary
- **Total tests written**: 3 new test files, 16 test cases
- **Total tests passing**: 16 in targeted runs
- **Layers used**: Unit (3)
- **Approval tests** (refactoring): None — no refactoring tasks
- **Pure functions created**: 1 (`AdoptionRequest.composeId`)

### Deviations from Design
None — implementation matches design.

### Issues Found
- Duplicate `@Transactional` on `AdoptionRequestService.reject` initially caused compilation error; removed duplicate annotation.

### Remaining Tasks
- [ ] 3.1 RED: add `Neo4jAdoptionRequestAdapterIntegrationTest` for MERGE adoption request, ownership check, LIKE check, and MATCHED creation.
- [ ] 3.2 GREEN: implement `Neo4jAdoptionRequestAdapter` + `AdoptionRequestNeo4jRepository` Cypher to pass integration tests.
- [ ] 3.3 Update `PetNeo4jRepository` with ownership query and `SwipeNeo4jRepository` with LIKE existence query used by adapter.
- [ ] 3.4 RED: add web tests for accept/reject endpoints with ROLE_REFUGIO and expected 403 on AuthorizationException.
- [ ] 3.5 GREEN: create `AdoptionRequestController`, `AdoptionDecisionRequest` (Bean Validation), update `SecurityConfig` to restrict endpoints, and `GlobalExceptionHandler` mapping to 403.

### Workload / PR Boundary
- Mode: stacked PR slice
- Current work unit: PR1 (domain + application + unit tests)
- Boundary: Domain model, ports, application services, and SwipeService update with unit tests only.
- Estimated review budget impact: ~250–320 lines

### Status
10/15 tasks complete. Ready for next batch.
