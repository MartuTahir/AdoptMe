# Tasks: Módulo 3 (Alta de Refugio) y Módulo 4 (Alta de Mascota)

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | 550-700 |
| 400-line budget risk | High |
| Chained PRs recommended | Yes |
| Suggested split | Chained PRs (PR 1: Módulo 3, PR 2: Módulo 4) |
| Delivery strategy | ask-on-risk |
| Chain strategy | stacked-prs |

Decision needed before apply: Yes (User must approve the division of tasks into 2 stacked PRs)
Chained PRs recommended: Yes
Chain strategy: stacked-prs
400-line budget risk: High

---

## PR 1: Módulo 3 - Alta de Refugio

### Phase 1: Domain & Application Layer (TDD Cycle)
- [x] 1.1 RED: Write unit test in `RegisterShelterServiceTest` using mocks for the persistence port to test successful registration and failure due to duplicate ID.
- [x] 1.2 GREEN: Create interfaces `RegisterShelterUseCase`, DTOs `RegisterShelterCommand`, `ShelterResult`, and `ShelterPersistencePort`.
- [x] 1.3 GREEN: Implement `RegisterShelterService` verifying existence before saving.

### Phase 2: Persistence Layer (TDD Cycle)
- [x] 2.1 RED: Write integration test in `Neo4jShelterAdapterIntegrationTest` using Testcontainers Neo4j to verify the insertion of `Shelter` and constraint violation handling.
- [x] 2.2 GREEN: Create `ShelterNeo4jRepository` extending `Neo4jRepository`.
- [x] 2.3 GREEN: Implement `Neo4jShelterAdapter` to satisfy `ShelterPersistencePort`.
- [x] 2.4 GREEN: Add Cypher constraint for `Shelter` unicidad in `src/main/resources/schema.cypher`.

### Phase 3: Web & Security Layer (TDD Cycle)
- [x] 3.1 RED: Write MockMvc integration tests in `ShelterControllerIntegrationTest` to verify successful status (201 Created), validation errors (400 Bad Request), and security access control (403 Forbidden).
- [x] 3.2 GREEN: Create `RegisterShelterRequest` DTO and `ShelterController` endpoint.
- [x] 3.3 GREEN: Modify `SecurityConfig.java` to restrict `/api/shelters/**` to the role `REFUGIO`.

---

## PR 2: Módulo 4 - Alta de Mascota (Validaciones)

### Phase 4: Persistence validation refinement (TDD Cycle)
- [x] 4.1 RED: Write integration test in `Neo4jPetAdapterIntegrationTest` verifying that saving a `Pet` with a non-existent shelter ID throws `EntityNotFoundException`.
- [x] 4.2 GREEN: Modify `Neo4jPetAdapter.java` to throw `EntityNotFoundException` (instead of `IllegalArgumentException`) when the shelter does not exist in Neo4j.

### Phase 5: Web Layer error mapping verification (TDD Cycle)
- [x] 5.1 RED: Write integration test in `PetControllerIntegrationTest` verifying that `POST /api/pets` returns `404 Not Found` when the shelter ID is invalid.
- [x] 5.2 Run the entire test suite with Maven to ensure 100% success across all components.
- [x] 5.3 Verify that the new code complies with hexagonal guidelines (domain has zero framework/DB dependencies).
