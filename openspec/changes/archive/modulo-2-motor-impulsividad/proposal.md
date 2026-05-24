# Proposal: Módulo 2 Motor de Impulsividad

## Intent
Implementar la persistencia real de los swipes en Neo4j y construir un mecanismo de control de impulsividad (Motor de Impulsividad) en el dominio para detectar y bloquear el comportamiento abusivo o automático de likes.

## Scope

### In Scope
- Crear `SwipeNeo4jRepository` y `Neo4jSwipeAdapter` para persistir la relación `(User)-[:SWIPED {action, timestamp}]->(Pet)`.
- Crear el servicio de dominio `ImpulsivityEngine` para evaluar límites de frecuencia de likes.
- Definir el límite de likes: **máximo 10 LIKEs por minuto**.
- Lanzar `ImpulsiveBehaviorException` si se detecta comportamiento impulsivo.
- Modificar `SwipeService` para aplicar el control antes de registrar el swipe.
- Pruebas unitarias e integración de base de datos (con Testcontainers) del motor.

### Out of Scope
- Lógica de impulsividad para acciones `DISLIKE` (solo aplica a `LIKE`).
- Rate limiting a nivel de red (API Gateway / Filtros de red).

## Capabilities

### New Capabilities
- `impulsivity-control`: Evalúa el comportamiento impulsivo de likes y bloquea temporalmente al adoptante.

### Modified Capabilities
- `matchpet-foundation`: Extiende la infraestructura para incluir el adaptador real de Neo4j para la entidad y relaciones `SWIPED`.

## Approach
- Mapear el swipe como relación directa de grafo usando Cypher en `SwipeNeo4jRepository`.
- Lógica de detección mediante consulta de recuento en Neo4j: `MATCH (u:User {id: $userId})-[r:SWIPED {action: 'LIKE'}]->(:Pet) WHERE r.timestamp >= $since RETURN count(r)`.
- Integrar la validación en el caso de uso `SwipeUseCase` lanzando una excepción de dominio que devuelva un código HTTP 400 o 429.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `src/main/java/com/matchpet/application/ports/output/SwipePersistencePort.java` | Modified | Añadir método `long countLikesSince(String userId, Instant since)`. |
| `src/main/java/com/matchpet/infrastructure/adapters/output/neo4j/Neo4jSwipeAdapter.java` | New | Adaptador SDN 7 para persistir y contar swipes. |
| `src/main/java/com/matchpet/infrastructure/adapters/output/neo4j/repositories/SwipeNeo4jRepository.java` | New | Repositorio Cypher de Neo4j para swipes. |
| `src/main/java/com/matchpet/domain/exception/ImpulsiveBehaviorException.java` | New | Excepción de dominio para impulsividad. |
| `src/main/java/com/matchpet/domain/service/ImpulsivityEngine.java` | New | Regla de negocio de la impulsividad. |
| `src/main/java/com/matchpet/application/services/SwipeService.java` | Modified | Validar impulsividad antes del guardado. |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Carga en BD por lecturas recurrentes | Med | Indexación de la propiedad `timestamp` en relaciones si crece el volumen. |

## Rollback Plan
Revertir con `git checkout` los archivos modificados y eliminar las clases e interfaces creadas.

## Dependencies
- Neo4j Database + Testcontainers.

## Success Criteria
- [ ] Compilación exitosa y cobertura del nuevo código ≥80%.
- [ ] Pruebas unitarias de `ImpulsivityEngine` verificando límites.
- [ ] Pruebas de integración de Neo4j confirmando el correcto conteo de relaciones `SWIPED` por tiempo.
