## Exploration: Módulo 3 (Alta de Refugio) y Módulo 4 (Alta de Mascota)

### Current State
El sistema actual cuenta con la capacidad de persistir una Mascota (`Pet`) vinculada a un Refugio (`Shelter`). Sin embargo, existe una limitación estructural crítica:
1. **No hay API ni mecanismo para crear un `Shelter`**: La persistencia de `Pet` en `Neo4jPetAdapter` valida que el `Shelter` exista en la base de datos antes de guardar la mascota, pero actualmente no existe ningún endpoint, servicio ni puerto para dar de alta un nodo `Shelter`.
2. **Falta de Constraints de Cypher para Shelter**: Mientras que `User`, `Pet` y `Trait` tienen restricciones de unicidad sobre su propiedad `id` en `schema.cypher`, la entidad `Shelter` carece de esta protección, lo que podría provocar la creación de identificadores duplicados en el grafo si no se controla adecuadamente.
3. **Mapeo de Mascota en el Controlador**: El endpoint `/api/pets` (en `PetController`) recibe un JSON con el refugio anidado completo (`ShelterRequest`), pero al no existir el refugio de antemano en Neo4j, cualquier llamada fallará en el adaptador con un `IllegalArgumentException: Shelter not found`.

### Affected Areas
- `src/main/resources/schema.cypher` — Agregar constraint de unicidad para `Shelter.id`.
- `src/main/java/com/matchpet/infrastructure/adapters/input/web/config/SecurityConfig.java` — Proteger los endpoints de refugios `/api/shelters/**` bajo el rol `REFUGIO`.
- `src/main/java/com/matchpet/domain/model/Shelter.java` — Mantener el record actual de dominio, asegurando validaciones de consistencia.
- `src/main/java/com/matchpet/application/ports/input/RegisterShelterUseCase.java` [NEW] — Interfaz para el caso de uso de registro de Refugios.
- `src/main/java/com/matchpet/application/ports/input/dto/RegisterShelterCommand.java` [NEW] — DTO de comando para el caso de uso.
- `src/main/java/com/matchpet/application/ports/input/dto/ShelterResult.java` [NEW] — DTO de resultado para el caso de uso.
- `src/main/java/com/matchpet/application/services/RegisterShelterService.java` [NEW] — Servicio de aplicación que coordina el registro de Refugios.
- `src/main/java/com/matchpet/application/ports/output/ShelterPersistencePort.java` [NEW] — Puerto de salida para persistencia de Refugios.
- `src/main/java/com/matchpet/infrastructure/adapters/output/neo4j/repositories/ShelterNeo4jRepository.java` [NEW] — Repositorio Spring Data Neo4j para la entidad `Shelter`.
- `src/main/java/com/matchpet/infrastructure/adapters/output/neo4j/Neo4jShelterAdapter.java` [NEW] — Adaptador de persistencia Neo4j para `Shelter`.
- `src/main/java/com/matchpet/infrastructure/adapters/input/web/dto/RegisterShelterRequest.java` [NEW] — DTO REST de entrada para la creación de refugios.
- `src/main/java/com/matchpet/infrastructure/adapters/input/web/controllers/ShelterController.java` [NEW] — Controlador REST expuesto en `/api/shelters` para registrar refugios.

---

### Approaches

#### 1. Implementación en Cadena Acoplada (Shelter Inline con Pet)
Permitir que al dar de alta una mascota (`POST /api/pets`), si el refugio no existe, se cree automáticamente en ese mismo instante de forma transaccional.
- **Pros**:
  - Un único endpoint para resolver ambas entidades en una sola petición del cliente web.
- **Cons**:
  - Rompe el principio de responsabilidad única del caso de uso de mascotas.
  - Genera inconsistencias de diseño: un refugio suele crearse una sola vez, mientras que las mascotas se registran continuamente.
  - Aumenta el riesgo de crear refugios fantasma o duplicados por errores tipográficos en el cliente web.
- **Effort**: Low

#### 2. Casos de Uso Desacoplados con Control de Existencia Expresa (Recomendado)
Separar estrictamente el ciclo de vida:
1. El refugio se registra primero de manera independiente mediante `POST /api/shelters`.
2. Una vez registrado el refugio en el sistema, el usuario con rol `REFUGIO` puede dar de alta la mascota en `POST /api/pets`, validando únicamente que el ID del refugio exista en Neo4j (comportamiento que ya está parcialmente esbozado en `Neo4jPetAdapter`).
- **Pros**:
  - Respeta fielmente la arquitectura limpia y la separación de conceptos.
  - Permite validar la existencia del refugio de forma aislada y transaccional.
  - Facilita pruebas unitarias independientes para cada caso de uso.
- **Cons**:
  - Requiere que el cliente web realice dos llamadas secuenciales si es una carga conjunta, aunque esto es lo correcto para la consistencia del dominio.
- **Effort**: Medium

---

### Recommendation
Se recomienda el **Approach 2 (Casos de Uso Desacoplados)**.
Debemos implementar toda la estructura hexagonal para el alta de Refugios (Módulo 3) y asegurar que el alta de Mascota (Módulo 4) valide la existencia previa del refugio a nivel de servicio de aplicación (o puerto de persistencia).
Además, añadiremos la restricción de unicidad para `Shelter.id` en `schema.cypher` para evitar duplicados en la base de datos de Neo4j.

---

### Risks
- **Autenticación y Roles**: La creación de refugios debe estar protegida para asegurar que solo los usuarios del refugio o administradores puedan dar de alta un refugio físico. Como el rol `REFUGIO` ya está contemplado, utilizaremos `PreAuthorize("hasRole('REFUGIO')")` en el controlador para asegurar el endpoint.
- **Mascotas Huérfanas**: Si falla la existencia del refugio, la transacción de mascota debe revertirse completamente.

---

### Ready for Proposal
**Yes**. La estrategia está clara. Proponemos avanzar a la creación de la propuesta formal para recibir feedback del usuario.
