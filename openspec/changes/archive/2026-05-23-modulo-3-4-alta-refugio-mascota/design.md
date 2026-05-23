# Design: Módulo 3 (Alta de Refugio) y Módulo 4 (Alta de Mascota)

## Technical Approach
Implementar los requerimientos de forma desacoplada bajo Arquitectura Hexagonal. 
Para el **Módulo 3**, el flujo sigue el patrón establecido de creación de entidades, utilizando records para los DTOs y mapeando la entidad `Shelter` de dominio pura mediante Spring Data Neo4j (SDN 7).
Para el **Módulo 4**, modificaremos la validación en el adaptador de mascotas (`Neo4jPetAdapter`) para que lance la excepción semántica de dominio `EntityNotFoundException` cuando se intente vincular una mascota a un refugio inexistente. Esto permitirá que la API web devuelva un error HTTP 404 (Not Found) autodescriptivo.

## Architecture Decisions

| Decision | Choices Considered | Selected | Rationale |
| :--- | :--- | :--- | :--- |
| **Validación de Refugio en Mascota** | Validar en `RegisterPetService` (capa aplicación) vs Validar en `Neo4jPetAdapter` (capa infraestructura) | **Validar en `Neo4jPetAdapter`** | Dado que la base de datos es la fuente de verdad del grafo, delegar la verificación de existencia en el adaptador permite centralizar la transacción y seguridad del grafo en el punto de persistencia, además de reutilizar la consulta optimizada ya existente `existsShelterById`. |
| **Excepción de Refugio Inexistente** | `IllegalArgumentException` vs `EntityNotFoundException` | **`EntityNotFoundException`** | El error semántico correcto para un recurso referenciado que no existe es 404. Cambiar la excepción a `EntityNotFoundException` en la persistencia de mascotas asegura que se retorne 404 en vez de 400. |
| **Constraint de Unicidad para Shelter** | Validar duplicados programáticamente vs Constraint en Base de Datos | **Ambos (Doble Capa)** | Se agrega constraint de unicidad en Cypher en el inicio (`schema.cypher`) y se valida programáticamente en el adaptador/servicio antes de persistir, evitando llamadas costosas de base de datos que fallen a nivel transaccional. |

## Data Flow

### Registro de Refugio (Módulo 3)
```
[Web Client] ──(RegisterShelterRequest)──> [ShelterController]
                                                │
                                      (RegisterShelterCommand)
                                                │
                                                ▼
[ShelterPersistencePort] <─── [RegisterShelterService]
           │                                    │
      (save/find)                         (ShelterResult)
           │                                    │
           ▼                                    ▼
       [Neo4j DB]                       [ShelterController]
```

### Registro de Mascota (Módulo 4)
```
[Web Client] ──(RegisterPetRequest)──> [PetController]
                                            │
                                  (RegisterPetCommand)
                                            │
                                            ▼
[PetPersistencePort] <─── [RegisterPetService]
           │                                │
      (save/find)                      (PetResult)
           │                                │
           ▼                                ▼
       [Neo4j DB]                      [PetController]
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `src/main/resources/schema.cypher` | Modify | Agregar `CREATE CONSTRAINT shelter_id_unique IF NOT EXISTS FOR (s:Shelter) REQUIRE s.id IS UNIQUE;`. |
| `src/main/java/com/matchpet/infrastructure/adapters/input/web/config/SecurityConfig.java` | Modify | Configurar el matcher `/api/shelters/**` para requerir el rol `REFUGIO`. |
| `src/main/java/com/matchpet/infrastructure/adapters/output/neo4j/Neo4jPetAdapter.java` | Modify | Modificar la excepción lanzada al fallar la validación de refugio de `IllegalArgumentException` a `EntityNotFoundException`. |
| `src/main/java/com/matchpet/application/ports/input/RegisterShelterUseCase.java` | Create | Interfaz del puerto de entrada del caso de uso. |
| `src/main/java/com/matchpet/application/ports/input/dto/RegisterShelterCommand.java` | Create | Record DTO para el comando del caso de uso. |
| `src/main/java/com/matchpet/application/ports/input/dto/ShelterResult.java` | Create | Record DTO para la salida del caso de uso. |
| `src/main/java/com/matchpet/application/services/RegisterShelterService.java` | Create | Implementación del caso de uso. Valida duplicados y persiste. |
| `src/main/java/com/matchpet/application/ports/output/ShelterPersistencePort.java` | Create | Puerto de persistencia para refugios. |
| `src/main/java/com/matchpet/infrastructure/adapters/output/neo4j/repositories/ShelterNeo4jRepository.java` | Create | Repositorio SDN 7 para persistencia de `Shelter`. |
| `src/main/java/com/matchpet/infrastructure/adapters/output/neo4j/Neo4jShelterAdapter.java` | Create | Implementación de `ShelterPersistencePort` delegando en `ShelterNeo4jRepository`. |
| `src/main/java/com/matchpet/infrastructure/adapters/input/web/dto/RegisterShelterRequest.java` | Create | Record DTO para requests REST con validaciones. |
| `src/main/java/com/matchpet/infrastructure/adapters/input/web/controllers/ShelterController.java` | Create | REST Controller expuesto en `POST /api/shelters`. |

## Interfaces / Contracts

```java
// RegisterShelterUseCase (Application)
public interface RegisterShelterUseCase {
    ShelterResult execute(RegisterShelterCommand command);
}

// ShelterPersistencePort (Application)
public interface ShelterPersistencePort {
    Optional<Shelter> findById(String id);
    boolean existsById(String id);
    Shelter save(Shelter shelter);
}

// DTOs
public record RegisterShelterCommand(String id, String name, String location) {}
public record ShelterResult(String id, String name, String location) {}
```

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| **Unit** | `RegisterShelterService` | Pruebas unitarias mockeando `ShelterPersistencePort` para validar éxito y el rechazo por duplicados. |
| **Integration** | `ShelterController` | `MockMvc` con Spring Security mockeado para verificar los HTTP 201 (Created), 400 (Bad Request / validation) y 403 (Forbidden) si no posee rol. |
| **Integration** | `PetController` | `MockMvc` para validar que si el refugio no existe se responda HTTP 404 (Not Found). |
| **Integration** | `Neo4jShelterAdapter` | Integración con Neo4j (Testcontainers) para validar la inserción correcta de refugios en el grafo. |
| **Integration** | `Neo4jPetAdapter` | Integración con Neo4j (Testcontainers) para verificar que la excepción se aroje y que la mascota quede correctamente vinculada al refugio (`LOCATED_IN`). |

## Migration / Rollout
Se requiere aplicar la constraint de Cypher definida en `schema.cypher` en la base de datos destino al desplegar. No hay migraciones de datos requeridas ya que no hay registros previos.
