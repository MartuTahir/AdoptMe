# Tasks: Implementar Endpoints Faltantes

## Phase 1: Foundation (Ports + DTOs)

### 1.1 Add `findAll(int skip, int limit)` to `PetPersistencePort`
- **File**: `src/main/java/com/matchpet/application/ports/output/PetPersistencePort.java`
- **Action**: Add method signature
- **Complexity**: S

### 1.2 Add `findByUserIdAndStatus(String, AdoptionRequestStatus)` to `AdoptionRequestPersistencePort`
- **File**: `src/main/java/com/matchpet/application/ports/output/AdoptionRequestPersistencePort.java`
- **Action**: Add method signature
- **Complexity**: S

### 1.3 Create `MatchResult` DTO
- **File**: `src/main/java/com/matchpet/application/ports/input/dto/MatchResult.java`
- **Action**: New record with `requestId`, `petId`, `acceptedAt`
- **Complexity**: S

## Phase 2: Persistence Adapters

### 2.1 Implement `findAll` in `Neo4jPetAdapter`
- **File**: `src/main/java/com/matchpet/infrastructure/adapters/output/neo4j/Neo4jPetAdapter.java`
- **Action**: Add Cypher query `MATCH (p:Pet) RETURN p SKIP $skip LIMIT $limit`
- **Dependency**: 1.1
- **Complexity**: M

### 2.2 Implement `findByUserIdAndStatus` in `Neo4jAdoptionRequestAdapter`
- **File**: `src/main/java/com/matchpet/infrastructure/adapters/output/neo4j/Neo4jAdoptionRequestAdapter.java`
- **Action**: Add Cypher query for user matches
- **Dependency**: 1.2
- **Complexity**: M

## Phase 3: Application Services

### 3.1 Create `GetAllPetsUseCase` interface
- **File**: `src/main/java/com/matchpet/application/ports/input/GetAllPetsUseCase.java`
- **Action**: New interface with `List<PetResult> execute(int page, int size)`
- **Dependency**: 1.1
- **Complexity**: S

### 3.2 Create `GetAllPetsService` implementation
- **File**: `src/main/java/com/matchpet/application/services/GetAllPetsService.java`
- **Action**: Implement `GetAllPetsUseCase`, delegate to `PetPersistencePort.findAll()`, validate pagination
- **Dependency**: 3.1, 2.1
- **Complexity**: S

### 3.3 Create `GetUserMatchesUseCase` interface
- **File**: `src/main/java/com/matchpet/application/ports/input/GetUserMatchesUseCase.java`
- **Action**: New interface with `List<MatchResult> execute(String userId)`
- **Dependency**: 1.2
- **Complexity**: S

### 3.4 Create `UserMatchesService` implementation
- **File**: `src/main/java/com/matchpet/application/services/UserMatchesService.java`
- **Action**: Implement `GetUserMatchesUseCase`, delegate to `AdoptionRequestPersistencePort.findByUserIdAndStatus()`
- **Dependency**: 3.3, 2.2
- **Complexity**: S

## Phase 4: Controllers

### 4.1 Add `@GetMapping` to `PetController`
- **File**: `src/main/java/com/matchpet/infrastructure/adapters/input/web/controllers/PetController.java`
- **Action**: Add `@GetMapping` endpoint with `@RequestParam page`, `@RequestParam size`, inject `GetAllPetsUseCase`
- **Dependency**: 3.2
- **Complexity**: S

### 4.2 Add `@GetMapping("/me/matches")` to `UserController`
- **File**: `src/main/java/com/matchpet/infrastructure/adapters/input/web/controllers/UserController.java`
- **Action**: Add endpoint, get userId from `Authentication.getName()`, inject `GetUserMatchesUseCase`
- **Dependency**: 3.4
- **Complexity**: S

## Phase 5: Security

### 5.1 Update `SecurityConfig` — split `/api/pets/**` by HTTP method
- **File**: `src/main/java/com/matchpet/infrastructure/adapters/input/web/config/SecurityConfig.java`
- **Action**: Split `/api/pets/**` into `GET` (authenticated) and `POST` (REFUGIO role)
- **Complexity**: S

## Phase 6: Tests

### 6.1 Unit test `GetAllPetsService`
- **File**: `src/test/java/com/matchpet/application/services/GetAllPetsServiceTest.java`
- **Action**: Test pagination validation, delegation to port
- **Dependency**: 3.2
- **Complexity**: S

### 6.2 Unit test `UserMatchesService`
- **File**: `src/test/java/com/matchpet/application/services/UserMatchesServiceTest.java`
- **Action**: Test delegation to port, userId validation
- **Dependency**: 3.4
- **Complexity**: S

### 6.3 Integration test `PetController` GET
- **File**: `src/test/java/com/matchpet/web/PetControllerIntegrationTest.java`
- **Action**: Test GET /api/pets with auth, pagination, empty list
- **Dependency**: 4.1
- **Complexity**: M

### 6.4 Integration test `UserController` GET /me/matches
- **File**: `src/test/java/com/matchpet/web/UserControllerIntegrationTest.java`
- **Action**: Test GET /api/users/me/matches with auth, empty list
- **Dependency**: 4.2
- **Complexity**: M
