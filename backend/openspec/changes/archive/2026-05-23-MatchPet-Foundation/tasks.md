# Tasks: MatchPet-Foundation

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | 830-930 |
| 400-line budget risk | High |
| Chained PRs recommended | Yes |
| Suggested split | PR 1 (Foundation) → PR 2 (Core) → PR 3 (Tests) |
| Delivery strategy | auto-chain (resolved) |
| Chain strategy | stacked-to-main |

Decision needed before apply: No (resolved by orchestrator/user)
Chained PRs recommended: Yes
Chain strategy: stacked-to-main
400-line budget risk: High

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | Build config + Domain entities | PR 1 | Base: main; includes pom.xml, domain model, basic structure tests |
| 2 | Application layer + Ports | PR 2 | Base: PR 1; includes use cases, ports, unit tests for logic |
| 3 | Infrastructure adapters + Integration tests | PR 3 | Base: PR 2; includes Neo4j adapters, Testcontainers integration |

## Phase 1: Foundation / Build Setup

- [x] 1.1 Create `pom.xml` with Spring Boot 3.4.x parent, Java 21, SDN 7, Spring Security, Validation
- [x] 1.2 Add Neo4j driver and Testcontainers dependencies for integration tests
- [x] 1.3 Create base package structure: `com.adoptme.matchpet.{domain,application,infrastructure,web}`

## Phase 2: Domain Model (TDD Cycle)

- [x] 2.1 RED: Write test for `Trait` entity creation and equality
- [x] 2.2 GREEN: Create `src/main/java/com/adoptme/matchpet/domain/model/Trait.java` as `@Node` record with `@Id String id, String name`
- [x] 2.3 REFACTOR: Validate Trait immutability and null constraints
- [x] 2.4 RED: Write test for `User` entity with PREFERS relationship to Traits
- [x] 2.5 GREEN: Create `src/main/java/com/adoptme/matchpet/domain/model/User.java` as `@Node` record with `@Id String id, String name, @Relationship(type="PREFERS") List<Trait> preferences`
- [x] 2.6 REFACTOR: Ensure User deduplicates repeated traits
- [x] 2.7 RED: Write test for `Shelter` entity creation
- [x] 2.8 GREEN: Create `src/main/java/com/adoptme/matchpet/domain/model/Shelter.java` as `@Node` record with `@Id String id, String name, String location`
- [x] 2.9 RED: Write test for `Pet` entity with relationships to Shelter and Traits
- [x] 2.10 GREEN: Create `src/main/java/com/adoptme/matchpet/domain/model/Pet.java` as `@Node` record with `@Id String id, String name, @Relationship(type="LOCATED_IN") Shelter shelter, @Relationship(type="HAS_TRAIT") List<Trait> traits`
- [x] 2.11 REFACTOR: Validate Pet cannot exist without Shelter reference

## Phase 3: Domain Services (TDD Cycle)

- [x] 3.1 RED: Write test for `CompatibilityEngine.calculate()` with matching traits (score > 0)
- [x] 3.2 RED: Write test for `CompatibilityEngine.calculate()` with no matching traits (score = 0)
- [x] 3.3 GREEN: Create `src/main/java/com/adoptme/matchpet/domain/service/CompatibilityEngine.java` with `calculate(Set<Trait> userTraits, Set<Trait> petTraits)` returning score and matched traits
- [x] 3.4 REFACTOR: Extract CompatibilityResult as domain value object (record)

## Phase 4: Application Layer - Ports (TDD Cycle)

- [x] 4.1 Create `src/main/java/com/adoptme/matchpet/application/ports/in/GetCompatibilityUseCase.java` interface with `execute(String userId, String petId)`
- [x] 4.2 Create `src/main/java/com/adoptme/matchpet/application/ports/out/UserPersistencePort.java` with `findById(String id)` and `save(User user)`
- [x] 4.3 Create `src/main/java/com/adoptme/matchpet/application/ports/out/PetPersistencePort.java` with `findById(String id)` and `save(Pet pet)`
- [x] 4.4 RED: Write test for `GetCompatibilityUseCase` implementation with mocked ports
- [x] 4.5 GREEN: Create `src/main/java/com/adoptme/matchpet/application/service/GetCompatibilityService.java` implementing the use case, calling ports and delegating to CompatibilityEngine
- [x] 4.6 REFACTOR: Add validation for missing User or Pet (throw domain exception)

## Phase 5: Infrastructure Layer - Neo4j Adapters

- [x] 5.1 Create `src/main/java/com/adoptme/matchpet/infrastructure/adapters/out/neo4j/repositories/UserNeo4jRepository.java` extending `Neo4jRepository<User, String>`
- [x] 5.2 Create `src/main/java/com/adoptme/matchpet/infrastructure/adapters/out/neo4j/repositories/PetNeo4jRepository.java` extending `Neo4jRepository<Pet, String>`
- [x] 5.3 Create `src/main/java/com/adoptme/matchpet/infrastructure/adapters/out/neo4j/Neo4jUserAdapter.java` implementing `UserPersistencePort`, injecting `UserNeo4jRepository`
- [x] 5.4 Create `src/main/java/com/adoptme/matchpet/infrastructure/adapters/out/neo4j/Neo4jPetAdapter.java` implementing `PetPersistencePort`, injecting `PetNeo4jRepository`

## Phase 6: Integration Tests (Testcontainers)

- [x] 6.1 Create `src/test/java/com/adoptme/matchpet/infrastructure/Neo4jUserAdapterIntegrationTest.java` using Testcontainers Neo4j
- [x] 6.2 Test: Persist User with PREFERS relationships to Traits (Spec scenario: Alta de usuario con traits válidos)
- [x] 6.3 Test: Verify duplicate traits are stored only once (Spec scenario: Traits duplicados en la solicitud)
- [x] 6.4 Create `src/test/java/com/adoptme/matchpet/infrastructure/Neo4jPetAdapterIntegrationTest.java`
- [x] 6.5 Test: Persist Pet with LOCATED_IN relationship to Shelter (Spec scenario: Alta de mascota con refugio existente)
- [x] 6.6 Test: Verify Pet persistence fails when Shelter does not exist (Spec scenario: Refugio inexistente)
- [x] 6.7 Create end-to-end test for GetCompatibilityUseCase with real Neo4j (Spec scenarios: Match con/sin coincidencias)

## Phase 7: Configuration & Wiring

- [x] 7.1 Create `src/main/resources/application.yml` with Neo4j connection config (local or AuraDB)
- [x] 7.2 Create `src/main/java/com/adoptme/matchpet/MatchPetApplication.java` main class with `@SpringBootApplication`
- [x] 7.3 Create `src/main/java/com/adoptme/matchpet/infrastructure/config/Neo4jConfig.java` (N/A - Not needed for this phase)

## Phase 9: Web & Security Layer (TDD Cycle)

- [x] 9.1 RED: Write MockMvc integration tests for login, registration, protected swipe/match endpoints and role-based pet registration.
- [x] 9.2 GREEN: Implement Spring Security 6 config with AuthenticationManager, stateless JWT filter, and access rules (public auth/register; secure swipe/match; REFUGIO-only pet endpoints).
- [x] 9.3 GREEN: Create REST controllers in `infrastructure.adapters.input.web.controllers` for user, pet, swipe and recommendations using input ports only.
- [x] 9.4 GREEN: Add global `@RestControllerAdvice` to map validation/auth/domain errors to clean API responses.
- [x] 9.5 REFACTOR: Keep DTO mapping/validation in controllers and preserve business logic inside use cases.

## Phase 8: Verification & Cleanup

- [x] 8.1 Run `mvn clean compile` and verify build passes
- [x] 8.2 Run all tests and verify coverage ≥70% (Spec NFR)
- [x] 8.3 Verify no prohibited dependencies: domain must NOT import Spring/Neo4j/infrastructure packages
- [x] 8.4 Update README.md with setup instructions (Neo4j requirement, run commands)
