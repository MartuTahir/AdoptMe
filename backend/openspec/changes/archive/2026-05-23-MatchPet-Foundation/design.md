# Design: MatchPet-Foundation

## Technical Approach
La estrategia técnica se basa en una **Arquitectura Hexagonal (Puertos y Adaptadores)** para aislar el núcleo de negocio de las dependencias externas (Neo4j, Spring Framework). El modelo de dominio utilizará nodos y relaciones nativas de Neo4j (SDN 7) definidos en el paquete de dominio. Para el cálculo de compatibilidad, se implementará un servicio de dominio que realice la intersección de conjuntos de traits, garantizando que la lógica sea testeable sin necesidad de una base de datos real.

## Architecture Decisions

| Decision | Choice | Alternatives | Rationale |
| :--- | :--- | :--- | :--- |
| **Arquitectura** | Hexagonal (Ports & Adapters) | Layered (3-tier) | Permite cambiar Neo4j por otra DB o mockear la persistencia fácilmente para tests unitarios del negocio. |
| **Persistencia** | Spring Data Neo4j 7 (SDN 7) | Neo4j Cypher DSL / OGM | SDN 7 es el estándar actual para Spring Boot 3, soporta proyecciones y tipos inmutables. |
| **Modelado de Datos** | Grafo (Nodos y Relaciones) | Documental / Relacional | El dominio de "matching" es un problema de grafos (usuarios, mascotas, traits y sus conexiones). |
| **Comunicación** | REST con records Java 21 | DTOs clásicos con Lombok | Los `records` proporcionan inmutabilidad y reducen el boilerplate de forma nativa en Java 21. |

## Data Flow
El flujo de datos para el cálculo de compatibilidad diseñado en la fase de Spec:

1.  **Web**: Recibe `userId` y `petId`. Llama al puerto de entrada `GetCompatibilityUseCase`.
2.  **Application (Use Case)**: 
    *   Carga `User` (con sus `Trait` preferidos) vía `UserPersistencePort`.
    *   Carga `Pet` (con sus `Trait` asociados) vía `PetPersistencePort`.
    *   Delega al `CompatibilityEngine` (Domain Service) el cálculo.
3.  **Domain (CompatibilityEngine)**: 
    *   Obtiene el set de traits del usuario y el set de traits de la mascota.
    *   Calcula la intersección: `matched = userTraits ∩ petTraits`.
    *   Score = `matched.size()`.
4.  **Application**: Devuelve un `CompatibilityResult` con el score y los traits coincidentes.

## File Changes

| File | Action | Description |
| :--- | :--- | :--- |
| `pom.xml` | Create | Configuración de Spring Boot 3.4, Java 21, SDN 7, Security y Validation. |
| `src/main/java/com/adoptme/matchpet/domain/model/User.java` | Create | Entidad @Node para Usuario con relación `PREFERS` hacia Trait. |
| `src/main/java/com/adoptme/matchpet/domain/model/Pet.java` | Create | Entidad @Node para Mascota con relación `LOCATED_IN` (Shelter) y `HAS_TRAIT` (Trait). |
| `src/main/java/com/adoptme/matchpet/domain/model/Trait.java` | Create | Entidad @Node para rasgos/características. |
| `src/main/java/com/adoptme/matchpet/domain/model/Shelter.java` | Create | Entidad @Node para el Refugio. |
| `src/main/java/com/adoptme/matchpet/domain/service/CompatibilityEngine.java` | Create | Lógica pura de intersección de sets para el matching. |
| `src/main/java/com/adoptme/matchpet/application/ports/in/GetCompatibilityUseCase.java` | Create | Interfaz del caso de uso de compatibilidad. |
| `src/main/java/com/adoptme/matchpet/application/ports/out/UserPersistencePort.java` | Create | Puerto de salida para persistencia de User. |
| `src/main/java/com/adoptme/matchpet/application/ports/out/PetPersistencePort.java` | Create | Puerto de salida para persistencia de Pet. |
| `src/main/java/com/adoptme/matchpet/infrastructure/adapters/out/neo4j/Neo4jUserAdapter.java` | Create | Implementación del puerto UserPersistencePort usando SDN 7. |
| `src/main/java/com/adoptme/matchpet/infrastructure/adapters/out/neo4j/repositories/UserNeo4jRepository.java` | Create | Interfaz de repositorio SDN 7. |

## Interfaces / Contracts

```java
// Domain Entities
@Node
public record User(@Id String id, String name, @Relationship(type = "PREFERS") List<Trait> preferences) {}

@Node
public record Pet(@Id String id, String name, @Relationship(type = "LOCATED_IN") Shelter shelter, @Relationship(type = "HAS_TRAIT") List<Trait> traits) {}

// Use Case
public interface GetCompatibilityUseCase {
    CompatibilityResult execute(String userId, String petId);
}
```

## Testing Strategy

| Layer | What to Test | Approach |
| :--- | :--- | :--- |
| **Unit (Domain)** | CompatibilityEngine | Validar intersección de traits y cálculo de score (0 a N). |
| **Unit (Application)** | Use Cases | Mockear puertos de salida y verificar que se llama al motor de compatibilidad. |
| **Integration** | Neo4j Adapters | Usar Testcontainers para validar que las relaciones PREFERS y HAS_TRAIT se persisten correctamente. |

## Open Questions
- [ ] ¿Se requiere ponderación (weight) en los traits en una fase posterior? (No afecta este diseño inicial).
- [ ] ¿El Shelter debe ser un nodo independiente o una propiedad del Pet para esta fase? (Se diseña como nodo por extensibilidad).
