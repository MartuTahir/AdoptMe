# Technical Design: Implementar Endpoints Faltantes

## Architecture Overview
Seguir patrón hexagonal existente: Ports → Services → Adapters → Controllers

## Component Diagram

```
GET /api/pets
  ├── PetController (@GetMapping)
  │   └── GetAllPetsUseCase (input port)
  │       └── GetAllPetsService (implements use case)
  │           └── PetPersistencePort (output port)
  │               └── Neo4jPetAdapter (implements with Cypher)

GET /api/users/me/matches
  ├── UserController (@GetMapping("/me/matches"))
  │   └── GetUserMatchesUseCase (input port)
  │       └── UserMatchesService (implements use case)
  │           └── AdoptionRequestPersistencePort (output port)
  │               └── Neo4jAdoptionRequestAdapter (implements with Cypher)
```

## Sequence: GET /api/pets

```
Client → PetController: GET /api/pets?page=0&size=20
PetController → GetAllPetsService: execute(0, 20)
GetAllPetsService → PetPersistencePort: findAll(0, 20)
PetPersistencePort → Neo4jPetAdapter: findAll(0, 20)
Neo4jPetAdapter → Neo4j: MATCH (p:Pet) RETURN p SKIP 0 LIMIT 20
Neo4j → Neo4jPetAdapter: List<Pet>
Neo4jPetAdapter → PetPersistencePort: List<Pet>
PetPersistencePort → GetAllPetsService: List<Pet>
GetAllPetsService → PetController: List<PetResult>
PetController → Client: 200 [PetResult, ...]
```

## Sequence: GET /api/users/me/matches

```
Client → UserController: GET /api/users/me/matches
UserController → GetUserMatchesService: execute("user@email.com")
GetUserMatchesService → AdoptionRequestPersistencePort: findByUserIdAndStatus("user@email.com", ACCEPTED)
AdoptionRequestPersistencePort → Neo4jAdoptionRequestAdapter: findByUserIdAndStatus(...)
Neo4jAdoptionRequestAdapter → Neo4j: MATCH (u:User)-[:REQUESTED]->(ar:AdoptionRequest) WHERE u.id=$userId AND ar.status='ACCEPTED' RETURN ar
Neo4j → Neo4jAdoptionRequestAdapter: List<AdoptionRequest>
Neo4jAdoptionRequestAdapter → AdoptionRequestPersistencePort: List<AdoptionRequest>
AdoptionRequestPersistencePort → GetUserMatchesService: List<AdoptionRequest>
GetUserMatchesService → UserController: List<MatchResult>
UserController → Client: 200 [MatchResult, ...]
```

## Neo4j Queries

### Query 1: Find all pets (paginated)
```cypher
MATCH (p:Pet)
RETURN p
SKIP $skip LIMIT $limit
```

### Query 2: Find user matches
```cypher
MATCH (u:User)-[:REQUESTED]->(ar:AdoptionRequest)
WHERE u.id = $userId AND ar.status = 'ACCEPTED'
RETURN ar
```

## DTOs

### MatchResult (nuevo)
```java
public record MatchResult(
    String requestId,
    String petId,
    Instant acceptedAt
) {}
```

## Architecture Decisions

### Decision 1: PetPersistencePort.findAll usa skip/limit, no Pageable
**Rationale**: Mantener consistencia con `ChatPersistencePort.getChatHistory(String, int skip, int limit)` que ya usa este patrón. Evita acoplamiento a Spring Data Pageable en el dominio.

### Decision 2: MatchResult usa AdoptionRequestResult existente como base
**Rationale**: Reusar `AdoptionRequestResult` si los campos coinciden. Si no, crear `MatchResult` específico para el frontend.

### Decision 3: SecurityConfig split por método HTTP
**Rationale**: Spring Security permite `requestMatchers(HttpMethod.GET, "/api/pets/**")` vs `requestMatchers(HttpMethod.POST, "/api/pets/**")`. Esto permite GET para todos autenticados y POST solo para REFUGIO.
