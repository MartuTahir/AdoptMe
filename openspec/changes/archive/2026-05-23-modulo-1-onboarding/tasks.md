# Tasks: Módulo 1 Onboarding y Formulario de Control

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | 350-450 |
| 400-line budget risk | Medium |
| Chained PRs recommended | No |
| Suggested split | Single PR |
| Delivery strategy | ask-on-risk |
| Chain strategy | size-exception |

Decision needed before apply: No
Chained PRs recommended: No
Chain strategy: size-exception
400-line budget risk: Medium

## Phase 1: Domain Layer (TDD Cycle)

- [x] 1.1 RED: Write unit tests in `TrustScoreCalculatorTest` for score rules (max 100, fail visits = 0).
- [x] 1.2 GREEN: Create `OnboardingForm` record and `TrustScoreCalculator` service.
- [x] 1.3 REFACTOR: Refactor `TrustScoreCalculator` to optimize conditions.
- [x] 1.4 RED: Write unit test in `UserTest` verifying `User` can contain and default a `trustScore`.
- [x] 1.5 GREEN: Modify `User` record to add `Integer trustScore` and overloaded constructors.
- [x] 1.6 REFACTOR: Clean up record constructors and verify package builds.

## Phase 2: Application Layer (TDD Cycle)

- [x] 2.1 RED: Write unit test in `SubmitOnboardingFormServiceTest` with mock ports.
- [x] 2.2 GREEN: Create `OnboardingCommand`, `SubmitOnboardingFormUseCase`, and `SubmitOnboardingFormService`.
- [x] 2.3 REFACTOR: Clean up service orchestration and verify exceptions.

## Phase 3: Web Layer & Security (TDD Cycle)

- [x] 3.1 RED: Write MockMvc integration tests in `UserControllerIntegrationTest` (200, 400 Bad Request, auth checks).
- [x] 3.2 GREEN: Create `OnboardingRequest` DTO and add POST endpoint to `UserController`.
- [x] 3.3 GREEN: Configure Spring Security in `SecurityConfig.java` to protect the onboarding endpoint.
- [x] 3.4 REFACTOR: Refactor controllers to ensure correct exception mapping.

## Phase 4: Persistence Integration (TDD Cycle)

- [x] 4.1 RED: Write integration test in `Neo4jUserAdapterIntegrationTest` using Testcontainers to verify `trustScore` in Neo4j.
- [x] 4.2 GREEN: Update mapping and ensure `UserNeo4jRepository` persists the score attribute correctly.

## Phase 5: Verification & Cleanup

- [x] 5.1 Run all tests with Maven wrapper/exec to verify 100% success.
- [x] 5.2 Verify domain has no infrastructure/Spring dependencies.
- [x] 5.3 Verify test coverage of new code is ≥80%.
