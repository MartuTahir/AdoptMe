## Exploration: Módulo 5 - Motor de Matches

### Current State
- Existe flujo de `swipe` expuesto en `POST /api/swipes`: valida usuario/mascota, aplica control de impulsividad en `LIKE`, y persiste relación `SWIPED` con `action` y `timestamp` en Neo4j.
- No hay modelo/relación de `MATCH` ni endpoint para aceptación por refugio; el grafo hoy sólo registra swipes.
- No hay componentes de mensajería aún; el módulo 5 debe habilitarlos a futuro a partir del MATCH.

### Affected Areas
- `src/main/java/com/matchpet/application/services/SwipeService.java` — hoy registra swipes; el match debe construirse sobre esta intención previa.
- `src/main/java/com/matchpet/infrastructure/adapters/output/neo4j/repositories/SwipeNeo4jRepository.java` — relación `SWIPED` actual; habrá que consultar/verificar `LIKE` previo.
- `src/main/java/com/matchpet/infrastructure/adapters/input/web/controllers/SwipeController.java` — endpoint actual; podría mantenerse como origen del flujo.
- Nuevos (a definir): `Match`/`AdoptionRequest` en dominio, puertos de persistencia, casos de uso de aceptación, y adaptadores Neo4j/web.

### Approaches
1. **MATCH directo al aceptar** — crear relación `MATCHED` entre `User` y `Pet` cuando el refugio acepta un `LIKE`.
   - Pros: modelo simple, bajo acoplamiento inicial, menor cantidad de nodos/relaciones.
   - Cons: falta rastro explícito del “pedido”; más difícil auditar estados (pendiente/rechazado).
   - Effort: Medium

2. **AdoptionRequest con estados + MATCH derivado** — crear entidad/relación `ADOPTION_REQUEST` (PENDING/ACCEPTED/REJECTED). Al aceptar, generar `MATCHED`.
   - Pros: workflow explícito, auditabilidad, base clara para mensajería futura.
   - Cons: más modelos y persistencia, requiere reglas de unicidad y transiciones.
   - Effort: Medium/High

### Recommendation
Adoptar **AdoptionRequest con estados + MATCH derivado**. El módulo 5 define un ciclo “like → aceptación”, y ese estado intermedio es clave para trazabilidad y futuras reglas (mensajería, SLA, reporting). Mantiene el grafo consistente y extensible.

### Risks
- Duplicación de matches si no se fuerza unicidad por `(userId, petId)`.
- Validación de autorización: asegurar que sólo el refugio dueño de la mascota pueda aceptar.
- Consistencia temporal: aceptar un `LIKE` inexistente o eliminado.

### Ready for Proposal
Yes — confirmar con el usuario el input mínimo del “aceptar” (IDs, rol, y reglas de idempotencia) y si el MATCH debe ser relación o nodo.
