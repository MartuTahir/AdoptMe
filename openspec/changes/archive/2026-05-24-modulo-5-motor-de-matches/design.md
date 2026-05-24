# Design: Módulo 5 - Motor de Matches

## Technical Approach

Implement AdoptionRequest como entidad de dominio con estado (PENDING/ACCEPTED/REJECTED). En el flujo de swipe LIKE, el caso de uso crea/vincula una solicitud idempotente. Refugios aceptan/rechazan solicitudes vía endpoints dedicados; aceptar valida ownership, existencia de LIKE y crea relación MATCHED. Persistencia en Neo4j con adaptadores y queries Cypher específicas manteniendo hexagonal.

## Architecture Decisions

| Option | Tradeoff | Decision |
|---|---|---|
| Modelar AdoptionRequest como **node** dedicado con relaciones a User/Pet | Más nodos/queries, pero entidad explícita y extensible (mensajería futura) | **Elegido**: nodo `AdoptionRequest` con id compuesto `(userId, petId)` y relaciones a User/Pet | 
| Modelar AdoptionRequest como **relationship** User→Pet con propiedades | Menos nodos, pero entidad implícita y menos extensible | No elegido por evolución futura y trazabilidad | 
| Crear AdoptionRequest en **SwipeService** (application) | Orquesta reglas y persistencia, respeta hex | **Elegido**: mantener reglas en application y usar puerto output | 
| Crear AdoptionRequest en adaptador Neo4j | Menos lógica en service, pero mezcla reglas con infraestructura | No elegido | 
| Transiciones ACCEPT/REJECT con @Transactional en service | Asegura atomicidad status + MATCHED | **Elegido** | 

## Data Flow

**Swipe LIKE → AdoptionRequest**

    SwipeController
        → SwipeUseCase (SwipeService)
            → SwipePersistencePort.save(SWIPED)
            → AdoptionRequestPersistencePort.createOrGetPending(userId, petId)
                → Neo4j AdoptionRequest adapter (MERGE AdoptionRequest + relaciones)

**Aceptar/Rechazar**

    AdoptionRequestController
        → Accept/Reject UseCase (AdoptionRequestService)
            → validar ownership refugio-pet
            → validar SWIPED LIKE
            → actualizar status
            → crear MATCHED (solo ACCEPTED)
            → devolver DTO

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `src/main/java/com/matchpet/domain/model/AdoptionRequest.java` | Create | Entidad de dominio con estado y timestamps | 
| `src/main/java/com/matchpet/domain/model/AdoptionRequestStatus.java` | Create | Enum de estados | 
| `src/main/java/com/matchpet/domain/exception/AuthorizationException.java` | Create | Error de autorización (refugio sin ownership) | 
| `src/main/java/com/matchpet/application/ports/input/AcceptAdoptionRequestUseCase.java` | Create | Puerto input aceptar | 
| `src/main/java/com/matchpet/application/ports/input/RejectAdoptionRequestUseCase.java` | Create | Puerto input rechazar | 
| `src/main/java/com/matchpet/application/ports/input/dto/AdoptionDecisionCommand.java` | Create | DTO request para aceptar/rechazar | 
| `src/main/java/com/matchpet/application/ports/input/dto/AdoptionRequestResult.java` | Create | DTO response | 
| `src/main/java/com/matchpet/application/ports/output/AdoptionRequestPersistencePort.java` | Create | Puerto output de persistencia | 
| `src/main/java/com/matchpet/application/services/AdoptionRequestService.java` | Create | Servicio application con @Transactional | 
| `src/main/java/com/matchpet/application/services/SwipeService.java` | Modify | En LIKE crea/vincula AdoptionRequest | 
| `src/main/java/com/matchpet/infrastructure/adapters/output/neo4j/Neo4jAdoptionRequestAdapter.java` | Create | Implementa puerto de persistencia | 
| `src/main/java/com/matchpet/infrastructure/adapters/output/neo4j/repositories/AdoptionRequestNeo4jRepository.java` | Create | Queries Cypher (MERGE, status, MATCHED) | 
| `src/main/java/com/matchpet/infrastructure/adapters/output/neo4j/repositories/SwipeNeo4jRepository.java` | Modify | Query para validar LIKE existente | 
| `src/main/java/com/matchpet/infrastructure/adapters/output/neo4j/repositories/PetNeo4jRepository.java` | Modify | Query para validar ownership refugio→pet | 
| `src/main/java/com/matchpet/infrastructure/adapters/input/web/controllers/AdoptionRequestController.java` | Create | Endpoints accept/reject | 
| `src/main/java/com/matchpet/infrastructure/adapters/input/web/dto/AdoptionDecisionRequest.java` | Create | DTO web con Bean Validation | 
| `src/main/java/com/matchpet/infrastructure/adapters/input/web/exception/GlobalExceptionHandler.java` | Modify | Map AuthorizationException → 403 | 
| `src/main/java/com/matchpet/infrastructure/adapters/input/web/config/SecurityConfig.java` | Modify | Restringir endpoints a ROLE_REFUGIO | 
| `src/test/java/com/matchpet/application/services/AdoptionRequestServiceTest.java` | Create | Unit tests del caso de uso | 
| `src/test/java/com/matchpet/infrastructure/Neo4jAdoptionRequestAdapterIntegrationTest.java` | Create | Integración con Testcontainers | 
| `src/test/java/com/matchpet/application/services/SwipeServiceTest.java` | Modify | Verifica creación idempotente de request en LIKE | 

## Interfaces / Contracts

```java
public interface AdoptionRequestPersistencePort {
    AdoptionRequest createOrGetPending(String userId, String petId);
    AdoptionRequest accept(String requestId, String shelterId);
    AdoptionRequest reject(String requestId, String shelterId);
}
```

```java
public record AdoptionDecisionCommand(String requestId, String shelterId) {}
public record AdoptionRequestResult(String requestId, String userId, String petId,
                                    AdoptionRequestStatus status, Instant updatedAt) {}
```

```java
@PostMapping("/api/adoption-requests/accept")
ResponseEntity<AdoptionRequestResult> accept(@Valid @RequestBody AdoptionDecisionRequest request)
```

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Unit | State transitions, idempotencia, errores de autorización/estado | JUnit + mocks en `AdoptionRequestServiceTest` | 
| Integration | Cypher MERGE de AdoptionRequest, ownership, LIKE, MATCHED | `@DataNeo4jTest` + Testcontainers (nuevo adapter test) | 
| Web | Endpoints accept/reject con seguridad | Spring Boot test (mock auth / roles) | 

## Migration / Rollout

No migration required. Se crean nodos/relaciones nuevas al ejecutar el flujo. No se backfillean likes históricos.

## Open Questions

- [ ] ¿Cómo se obtiene `shelterId` del refugio autenticado (JWT vs request body) para validar ownership sin spoofing?
- [ ] ¿Se requiere exponer endpoint para listar AdoptionRequests pendientes por refugio en este módulo?
