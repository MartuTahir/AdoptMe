# Tasks: Módulo 2 Motor de Impulsividad

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | 200-250 |
| 400-line budget risk | Low |
| Chained PRs recommended | No |
| Suggested split | Single PR |
| Delivery strategy | ask-on-risk |
| Chain strategy | size-exception |

Decision needed before apply: No
Chained PRs recommended: No
Chain strategy: size-exception
400-line budget risk: Low

## Phase 1: Domain Layer (TDD Cycle)

- [x] 1.1 RED: Write unit tests in `ImpulsivityEngineTest` validating max limit (10 likes) and exception throwing.
- [x] 1.2 GREEN: Create `ImpulsiveBehaviorException` and `ImpulsivityEngine` class.
- [x] 1.3 REFACTOR: Refactor `ImpulsivityEngine` constants and structures.

## Phase 2: Application Layer (TDD Cycle)

- [x] 2.1 RED: Write unit test in `SwipeServiceTest` to verify that `SwipeService` calls the count method on `SwipePersistencePort` and validates impulsivity.
- [x] 2.2 GREEN: Add method `countLikesSince` to `SwipePersistencePort` and update `SwipeService` implementation to integrate the check.
- [x] 2.3 REFACTOR: Clean up mock definitions in tests.

## Phase 3: Persistence Adapter Layer (TDD Cycle)

- [x] 3.1 RED: Write integration tests in `Neo4jSwipeAdapterIntegrationTest` using Testcontainers to verify relationship creation and temporal count.
- [x] 3.2 GREEN: Create `SwipeNeo4jRepository` interface and `Neo4jSwipeAdapter` adapter class.
- [x] 3.3 REFACTOR: Clean up custom Cypher queries and optimize mapping.

## Phase 4: Verification & Cleanup

- [x] 4.1 Run all tests with Maven to verify 100% success.
- [x] 4.2 Verify domain doesn't depend on Spring or Neo4j.
- [x] 4.3 Verify test coverage of new code is ≥80%.
