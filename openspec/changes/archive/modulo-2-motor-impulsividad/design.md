# Design: Módulo 2 Motor de Impulsividad

## Technical Approach
Implementar la detección de impulsividad en el dominio mediante un servicio `ImpulsivityEngine`. La verificación de la frecuencia de likes se realizará consultando a Neo4j el conteo de relaciones `SWIPED` creadas por el usuario en el último minuto mediante consultas Cypher optimizadas. Se construirá el adaptador real de persistencia para swipes en Neo4j usando SDN 7.

## Architecture Decisions

| Decision | Choices Considered | Selected | Rationale |
| :--- | :--- | :--- | :--- |
| **Persistencia de Relación** | Nodo intermedio SwipeEvent vs Relación directa `SWIPED` | **Relación directa `SWIPED`** | Simplifica el modelo en grafos: une directamente a `User` con `Pet` con atributos `action` y `timestamp`, siendo la forma natural de representar interacciones en grafos. |
| **Control de Frecuencia** | Contador en base de datos vs Rate limiter en memoria | **Contador en base de datos** | Es consistente ante múltiples réplicas del servicio y reinicios, y aprovecha las relaciones temporales indexables en Neo4j (BDII). |

## Data Flow
El flujo de datos para validar y registrar un swipe es:

```
[UserController] ──(SwipeRequest)──> [SwipeService]
                                           │
                              1. Contar likes desde Neo4j
                                           │
                                           ▼
[ImpulsivityEngine] <──(recentLikes)── [SwipeService]
          │
    2. ¿Impulsivo?
          │
          ├── [Sí] ──> Lanzar ImpulsiveBehaviorException
          │
          └── [No] ──> [SwipePersistencePort] ──> Persistir en Neo4j
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `src/main/java/com/matchpet/application/ports/output/SwipePersistencePort.java` | Modify | Añadir método `long countLikesSince(String userId, Instant since)`. |
| `src/main/java/com/matchpet/infrastructure/adapters/output/neo4j/repositories/SwipeNeo4jRepository.java` | Create | Repositorio SDN 7 con consultas `@Query` Cypher para crear relación y contar. |
| `src/main/java/com/matchpet/infrastructure/adapters/output/neo4j/Neo4jSwipeAdapter.java` | Create | Adaptador que implementa `SwipePersistencePort`. |
| `src/main/java/com/matchpet/domain/exception/ImpulsiveBehaviorException.java` | Create | Excepción de dominio de tipo runtime. |
| `src/main/java/com/matchpet/domain/service/ImpulsivityEngine.java` | Create | Servicio que define y evalúa el límite de 10 likes por minuto. |
| `src/main/java/com/matchpet/application/services/SwipeService.java` | Modify | Inyectar `ImpulsivityEngine` y validar impulsividad antes de guardar. |

## Interfaces / Contracts

```java
// SwipeNeo4jRepository (SDN 7)
public interface SwipeNeo4jRepository extends Neo4jRepository<User, String> {
    @Query("MATCH (u:User {id: $userId}), (p:Pet {id: $petId}) CREATE (u)-[r:SWIPED {action: $action, timestamp: $timestamp}]->(p)")
    void saveSwipe(String userId, String petId, String action, Instant timestamp);

    @Query("MATCH (u:User {id: $userId})-[r:SWIPED {action: 'LIKE'}]->(:Pet) WHERE r.timestamp >= $since RETURN count(r)")
    long countLikesSince(String userId, Instant since);
}
```

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| **Unit** | `ImpulsivityEngine` | Validar lanzamiento de excepción al superar el límite de 10. |
| **Unit** | `SwipeService` | Mockear puertos y verificar la interacción lógica de la impulsividad. |
| **Integration** | `Neo4jSwipeAdapter` | Usar Testcontainers Neo4j para persistir relaciones de swipe y validar que el recuento de likes temporales en el grafo funciona correctamente. |

## Migration / Rollout
No se requiere migración. Las relaciones `SWIPED` nuevas comenzarán a crearse con la propiedad `timestamp` correspondiente.
