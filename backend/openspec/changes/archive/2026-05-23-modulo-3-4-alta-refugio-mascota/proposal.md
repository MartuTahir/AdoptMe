# Proposal: Módulo 3 (Alta de Refugio) y Módulo 4 (Alta de Mascota)

## Intent
Implementar el caso de uso y endpoints REST para registrar Refugios (Módulo 3) y asociar Mascotas a Refugios existentes de forma consistente en Neo4j (Módulo 4), manteniendo la separación arquitectónica y aplicando tests automatizados.

## Scope

### In Scope
- **Módulo 3: Alta de Refugio**
  - Crear puerto de entrada `RegisterShelterUseCase` and DTOs (`RegisterShelterCommand`, `ShelterResult`).
  - Crear el servicio `RegisterShelterService` en la capa de aplicación.
  - Crear el puerto de salida `ShelterPersistencePort` para modular persistencia de Refugios.
  - Crear `ShelterNeo4jRepository` que extienda `Neo4jRepository`.
  - Crear `Neo4jShelterAdapter` que implemente el puerto de persistencia.
  - Exponer endpoint `POST /api/shelters` en `ShelterController` protegido por rol `REFUGIO`.
  - Agregar constraint de unicidad en Neo4j para `Shelter.id` en `schema.cypher`.
- **Módulo 4: Alta de Mascota**
  - Asegurar la validación de la existencia previa del refugio. Si se intenta registrar una mascota para un refugio inexistente, lanzar un error de dominio de negocio.
  - Validar que el endpoint `POST /api/pets` maneje correctamente las excepciones y retorne un código de estado adecuado (ej. 400 Bad Request o 404 Not Found, según corresponda) cuando el refugio no exista.

### Out of Scope
- Interfaz gráfica o frontend.
- Carga masiva de mascotas y refugios mediante archivos (CSV/Excel).

## Capabilities

### New Capabilities
- `shelter-registration`: Permite registrar nuevos refugios en el sistema con su correspondiente identificador único, nombre y ubicación física.
- `pet-registration-with-validation`: Extiende el registro de mascotas validando la existencia e integridad de la relación con el refugio en la base de datos de grafos.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `src/main/resources/schema.cypher` | Modified | Añadir restricción `CREATE CONSTRAINT shelter_id_unique`. |
| `src/main/java/com/matchpet/infrastructure/adapters/input/web/config/SecurityConfig.java` | Modified | Proteger endpoint `/api/shelters/**` bajo rol `REFUGIO`. |
| `src/main/java/com/matchpet/application/ports/input/RegisterShelterUseCase.java` | New | Puerto de entrada del caso de uso. |
| `src/main/java/com/matchpet/application/ports/input/dto/RegisterShelterCommand.java` | New | DTO de comando de entrada. |
| `src/main/java/com/matchpet/application/ports/input/dto/ShelterResult.java` | New | DTO de resultado de salida. |
| `src/main/java/com/matchpet/application/services/RegisterShelterService.java` | New | Implementación del caso de uso de registro de refugios. |
| `src/main/java/com/matchpet/application/ports/output/ShelterPersistencePort.java` | New | Puerto de persistencia de salida. |
| `src/main/java/com/matchpet/infrastructure/adapters/output/neo4j/repositories/ShelterNeo4jRepository.java` | New | Repositorio Spring Data Neo4j. |
| `src/main/java/com/matchpet/infrastructure/adapters/output/neo4j/Neo4jShelterAdapter.java` | New | Adaptador de persistencia. |
| `src/main/java/com/matchpet/infrastructure/adapters/input/web/dto/RegisterShelterRequest.java` | New | DTO de request REST. |
| `src/main/java/com/matchpet/infrastructure/adapters/input/web/controllers/ShelterController.java` | New | Endpoint POST /api/shelters. |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Carga de refugio nulo o vacío en alta de mascota | Low | Validaciones con `jakarta.validation` en el DTO de request. |
| Inconsistencias de IDs de refugios por concurrencia | Low | Restricción de unicidad en Neo4j (`CREATE CONSTRAINT`) que lance excepción nativa de persistencia capturada por el handler global. |

## Rollback Plan
- Revertir las modificaciones en los archivos existentes con `git checkout -- <archivo>`.
- Eliminar físicamente los archivos nuevos creados en la estructura del código.

## Success Criteria
- [ ] Compilación exitosa del proyecto.
- [ ] Cobertura de tests automatizados para el nuevo código ≥80% (unitarios e integración).
- [ ] Endpoint `POST /api/shelters` operativo y probado.
- [ ] El caso de uso de alta de mascota rechaza la creación si el ID de refugio es inexistente, arrojando error semántico de negocio.
