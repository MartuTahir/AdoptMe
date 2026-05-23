# Proposal: MatchPet-Foundation

## Intent
Establecer la base técnica del proyecto AdoptMe usando Spring Boot 3 y Java 21, definiendo una estructura de Arquitectura Hexagonal y el modelo de datos inicial en Neo4j para soportar el sistema de matching.

## Scope

### In Scope
- Creación de `pom.xml` con dependencias de Spring Boot 3, SDN 7, Security y Validation.
- Definición de la estructura de paquetes (domain, application, infrastructure, web).
- Entidades de dominio iniciales: `User`, `Pet`, `Trait`.
- Configuración base de Neo4j y Seguridad (JWT).

### Out of Scope
- Lógica de matching compleja.
- Implementación de controladores REST (solo estructura).
- Frontend.

## Capabilities

### New Capabilities
- `matchpet-foundation`: Estructura base y persistencia de grafos inicial.

### Modified Capabilities
None

## Approach
- **Stack**: Java 21 + Spring Boot 3.4.
- **Persistence**: Spring Data Neo4j 7 (SDN 7).
- **Architecture**: Hexagonal (Ports & Adapters) para desacoplar el dominio de la tecnología de persistencia.
- **Data Model**: Grafo con nodos `User`, `Pet`, y `Trait` interconectados por relaciones de propiedad y preferencia.
- **DTOs**: Uso exhaustivo de Java 21 `records` para inmutabilidad y claridad.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `pom.xml` | New | Configuración de dependencias y build. |
| `src/main/java/com/adoptme/matchpet/domain` | New | Modelos y servicios de dominio. |
| `src/main/java/com/adoptme/matchpet/application` | New | Casos de uso y puertos. |
| `src/main/java/com/adoptme/matchpet/infrastructure` | New | Adaptadores de persistencia (Neo4j) y configuración. |
| `src/main/java/com/adoptme/matchpet/web` | New | Controladores y DTOs. |

## Risks

| Risk | Likelihood | Mitigation |
|------|--------|-------------|
| Complejidad de SDN 7 con relaciones complejas | Med | Seguir patrones de Proyecciones y DTOs para evitar ciclos. |
| Curva de aprendizaje de Arquitectura Hexagonal | Low | Documentar claramente la responsabilidad de cada paquete. |

## Rollback Plan
Eliminar los archivos creados y revertir `pom.xml` (si existiera). Al ser un proyecto nuevo, el rollback es trivial.

## Dependencies
- Neo4j Database (Local o AuraDB).
- Java 21 JDK.

## Success Criteria
- [x] `pom.xml` compila correctamente con `mvn clean compile`.
- [x] Estructura de paquetes creada y siguiendo el patrón hexagonal.
- [x] Entidades de dominio `User`, `Pet` y `Trait` definidas.
