## Exploration: Módulo 2 Motor de Impulsividad

### Current State
El sistema cuenta con una interfaz `SwipePersistencePort` y una clase `SwipeService` que delega la persistencia a dicho puerto. Sin embargo, no existe ninguna implementación de este adaptador para Neo4j (está simulado mediante mocks en los tests). Tampoco hay ningún control de frecuencia o velocidad sobre los swipes (likes).

### Affected Areas
- `src/main/java/com/matchpet/application/ports/output/SwipePersistencePort.java` — Se requerirá agregar un método para contar likes recientes, por ejemplo: `long countLikesSince(String userId, Instant since)`.
- `src/main/java/com/matchpet/infrastructure/adapters/output/neo4j/Neo4jSwipeAdapter.java` [NEW] — Adaptador de persistencia para guardar los swipes y contar los recientes.
- `src/main/java/com/matchpet/infrastructure/adapters/output/neo4j/repositories/SwipeNeo4jRepository.java` [NEW] — Repositorio Neo4j para mapear la relación o ejecutar consultas Cypher personalizadas.
- `src/main/java/com/matchpet/domain/exception/ImpulsiveBehaviorException.java` [NEW] — Excepción de dominio cuando se detecta impulsividad.
- `src/main/java/com/matchpet/domain/service/ImpulsivityEngine.java` [NEW] — Motor de dominio que define los límites de impulsividad.
- `src/main/java/com/matchpet/application/services/SwipeService.java` — Modificación para validar impulsividad antes de registrar el swipe.

---

### Approaches

#### 1. Cypher-based Frequency Check in Neo4j (Consulta de Frecuencia en Grafo)
Cada vez que un usuario realiza un `LIKE`, el sistema ejecuta una consulta Cypher en Neo4j para contar las relaciones de tipo `SWIPED` con propiedad `action = 'LIKE'` creadas en el último minuto. Si el conteo supera el límite (por ejemplo, 10 likes), se bloquea y se lanza `ImpulsiveBehaviorException`.

- **Pros**:
  - Consistencia: Los swipes persisten en la base de datos y el conteo es preciso ante reinicios o despliegues.
  - Almacena el historial en el grafo, permitiendo analizar patrones de comportamiento de adopción en Neo4j.
  - Se alinea con los objetivos de un proyecto de Base de Datos (BDII) al utilizar consultas de relaciones y grafos.
- **Cons**:
  - Genera una consulta de lectura por cada escritura (swipe).
- **Effort**: Low

#### 2. Sliding Window Rate Limiter in Memory (Limitador de Ventana Deslizante en Memoria)
Utilizar un mapa en memoria (`ConcurrentHashMap`) con una cola de timestamps por usuario para llevar el registro de los últimos swipes en los últimos 60 segundos.

- **Pros**:
  - Excelente rendimiento y menor carga a la base de datos.
- **Cons**:
  - Pérdida de estado ante reinicios de la aplicación.
  - No escala horizontalmente en sistemas distribuidos.
- **Effort**: Medium

---

### Recommendation
Se recomienda el **Approach 1 (Cypher-based Frequency Check in Neo4j)**.
Dado que la aplicación está centrada en la persistencia en grafos y relaciones (Neo4j), registrar la acción de swipe como una relación directa `(u:User)-[:SWIPED {action: 'LIKE', timestamp: ...}]->(p:Pet)` y realizar la consulta de frecuencia a través de Neo4j es la solución ideal. Esto asegura que la lógica de negocio esté respaldada por la persistencia y sea consistente.
Se propone un límite inicial de:
- **Máximo 10 LIKEs en 1 minuto**.
- Si se supera, se lanza `ImpulsiveBehaviorException` (retornando un error HTTP `429 Too Many Requests` o `400 Bad Request` con mensaje descriptivo).

---

### Risks
- **Desempeño de la base de datos**: Un índice en la propiedad `timestamp` de la relación `SWIPED` es vital si el volumen de swipes crece.
- **Consistencia temporal**: Uso correcto de zonas horarias (`Instant.now()` y formato ISO en Neo4j).

---

### Ready for Proposal
**Yes**. El enfoque técnico está claro. Proponemos proceder con la creación de la propuesta y la especificación detallada del Motor de Impulsividad.
