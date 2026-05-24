# Proposal: Módulo 5 - Motor de Matches

## Intent

Habilitar el flujo “like → solicitud de adopción → aceptación → match” con trazabilidad, como base para mensajería futura, manteniendo la arquitectura hexagonal.

## Scope

### In Scope
- Modelo de dominio `AdoptionRequest` con estados (PENDING/ACCEPTED/REJECTED).
- Caso de uso de aceptación por refugio y creación de relación `MATCHED`.
- Validaciones: refugio dueño de la mascota, `LIKE` existente, idempotencia por (userId, petId).
- Persistencia Neo4j y endpoints web necesarios.
- Tests unit/integration para casos clave.

### Out of Scope
- Mensajería entre adoptante y refugio.
- UI/UX de aprobación/rechazo.
- Reglas avanzadas de ranking o SLA.

## Capabilities

### New Capabilities
- `adoption-requests`: ciclo de vida de solicitudes y generación de `MATCHED` al aceptar.

### Modified Capabilities
- `matchpet`: un `LIKE` **MUST** crear/vincular una `AdoptionRequest` en estado PENDING de forma idempotente.

## Approach

Adoptar “AdoptionRequest con estados + MATCH derivado”. En hexagonal: entidad y reglas en `domain`, casos de uso en `application`, puertos para persistencia/consulta, adaptadores Neo4j y web. Al aceptar, se valida ownership del refugio y existencia de `LIKE`, se transiciona a ACCEPTED y se crea `MATCHED`.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `src/main/java/com/matchpet/application/services/SwipeService.java` | Modified | Crear/vincular AdoptionRequest en `LIKE`.
| `src/main/java/com/matchpet/infrastructure/adapters/output/neo4j/repositories/SwipeNeo4jRepository.java` | Modified | Consultas para verificar `LIKE` y unicidad.
| `src/main/java/com/matchpet/infrastructure/adapters/input/web/controllers/SwipeController.java` | Modified | Orquestar creación de request en `LIKE`.
| `src/main/java/com/matchpet/application/services/` | New | Caso de uso de aceptación de request.
| `src/main/java/com/matchpet/domain/` | New | Entidad `AdoptionRequest` + reglas.
| `src/main/java/com/matchpet/infrastructure/adapters/output/neo4j/` | New | Persistencia de requests y `MATCHED`.
| `src/main/java/com/matchpet/infrastructure/adapters/input/web/controllers/` | New | Endpoint de aceptación.

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Duplicación de matches | Med | Constraint/chequeo de unicidad por (userId, petId).
| Aceptación sin autorización | Med | Validar refugio dueño de la mascota.
| Aceptar `LIKE` inexistente | Low | Verificar relación `SWIPED` antes de aceptar.

## Rollback Plan

Deshabilitar el endpoint de aceptación (feature flag o despublicación) y revertir el uso de `AdoptionRequest` en `LIKE`. Si es necesario, limpiar relaciones `ADOPTION_REQUEST`/`MATCHED` mediante script Cypher controlado.

## Dependencies

- Disponibilidad de identidad/rol de refugio y ownership de mascotas.

## Success Criteria

- [ ] Un `LIKE` crea/vincula `AdoptionRequest` PENDING (idempotente).
- [ ] Un refugio autorizado puede aceptar y se crea `MATCHED`.
- [ ] Rechazos por falta de `LIKE` o sin ownership pasan tests.
- [ ] Tests unit/integration pasan con arquitectura hexagonal intacta.
