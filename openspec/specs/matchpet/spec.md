# MatchPet-Foundation Specification

## Purpose
Definir el comportamiento mínimo del setup inicial para persistencia base en Neo4j y una consulta de compatibilidad simple, manteniendo separación por Arquitectura Hexagonal.

## Functional Requirements

### Requirement: Persistir Usuario con preferencias (Traits) y Score de Confianza
El sistema **MUST** permitir crear y actualizar un `User` con cero o más `Trait` de preferencia y su score de confianza. El sistema **MUST** persistir el nodo `User` (incluyendo su campo `trustScore`) y sus relaciones de preferencia de forma consistente en Neo4j.
(Previously: El sistema MUST permitir crear y actualizar un User con cero o más Trait de preferencia. El sistema MUST persistir el nodo User y sus relaciones de preferencia de forma consistente.)

#### Scenario: Alta de usuario con traits válidos
- **GIVEN** un usuario nuevo con identificador único y lista de traits válidos
- **WHEN** se ejecuta el caso de uso de persistencia de usuario
- **THEN** el usuario queda persistido en Neo4j
- **AND** quedan persistidas sus relaciones de preferencia hacia cada trait

#### Scenario: Traits duplicados en la solicitud
- **GIVEN** un usuario con traits repetidos en el request
- **WHEN** se persiste el usuario
- **THEN** el sistema guarda cada trait una sola vez por usuario

#### Scenario: Guardar score de confianza actualizado
- **GIVEN** un usuario persistido en el sistema
- **WHEN** se actualiza su score de confianza a un valor válido (0-100)
- **THEN** el valor del campo `trustScore` del nodo `User` se actualiza de forma consistente en Neo4j

### Requirement: Persistir Refugio independiente
El sistema **MUST** permitir persistir un `Shelter` con su identificador único, nombre y ubicación. El sistema **MUST** garantizar la unicidad de los ID de los refugios de forma consistente.

### Requirement: Persistir Mascota vinculada a Refugio
El sistema **MUST** permitir persistir una `Pet` vinculada a un `Shelter` (Refugio) existente. El sistema **MUST NOT** crear mascotas huérfanas sin vínculo de refugio.

#### Scenario: Alta de mascota con refugio existente
- **GIVEN** un refugio existente y una mascota válida
- **WHEN** se ejecuta el caso de uso de persistencia de mascota
- **THEN** la mascota queda persistida
- **AND** queda vinculada al refugio indicado

#### Scenario: Refugio inexistente
- **GIVEN** una solicitud de alta de mascota con id de refugio inexistente
- **WHEN** se intenta persistir la mascota
- **THEN** la operación falla con error de dominio validable

### Requirement: Consulta básica de compatibilidad por traits
El sistema **MUST** exponer una consulta de matching simple entre `User` and `Pet` basada en intersección de traits (sin ponderaciones avanzadas). El resultado **SHALL** devolver score simple y detalle de traits coincidentes.

#### Scenario: Match con coincidencias
- **GIVEN** un usuario y una mascota con traits superpuestos
- **WHEN** se ejecuta la consulta de compatibilidad
- **THEN** se devuelve score mayor a cero
- **AND** se listan los traits coincidentes

#### Scenario: Match sin coincidencias
- **GIVEN** un usuario y una mascota sin traits comunes
- **WHEN** se ejecuta la consulta de compatibilidad
- **THEN** se devuelve score igual a cero

### Requirement: Persistir Swipe (Interacción de Adopción)
El sistema **MUST** permitir persistir un `SwipeEvent` representando una interacción de tipo `LIKE` o `DISLIKE` de un usuario hacia una mascota. El sistema **MUST** guardar la relación `SWIPED` en el grafo de Neo4j conectando el nodo `User` con el nodo `Pet`, incluyendo las propiedades `action` y `timestamp` de forma consistente. El sistema **MUST** crear o vincular una `AdoptionRequest` en estado `PENDING` cuando la acción es `LIKE`, de forma idempotente por `(userId, petId)`.
(Previously: Persistía el `SwipeEvent` y la relación `SWIPED` con `action` y `timestamp`, sin crear/vincular `AdoptionRequest`.)

#### Scenario: Persistencia exitosa de un swipe LIKE
- **GIVEN** un usuario y una mascota existentes en el sistema
- **WHEN** se ejecuta el registro de un swipe con acción `LIKE`
- **THEN** se crea una relación `SWIPED` con propiedad `action = 'LIKE'` en el grafo de Neo4j
- **AND** la relación tiene el `timestamp` correcto en formato temporal
- **AND** se crea o vincula una `AdoptionRequest` `PENDING` idempotente

#### Scenario: Persistencia exitosa de un swipe DISLIKE
- **GIVEN** un usuario y una mascota existentes en el sistema
- **WHEN** se ejecuta el registro de un swipe con acción `DISLIKE`
- **THEN** se crea una relación `SWIPED` con propiedad `action = 'DISLIKE'` en el grafo de Neo4j

## Non-Functional Requirements

### Requirement: Cumplimiento de Clean/Hexagonal Architecture
El código **MUST** mantener dependencia unidireccional: `web/infrastructure -> application -> domain`; `domain` **MUST NOT** depender de frameworks ni adaptadores.

### Requirement: Calidad y cobertura de tests
La solución **MUST** incluir tests automatizados para todos los casos de uso descritos. La cobertura mínima **SHOULD** ser:
- Dominio y aplicación: 80% líneas.
- Módulo total: 70% líneas.

### Requirement: Persistencia consistente en grafo
Las operaciones de escritura **MUST** garantizar consistencia de nodos y relaciones en una unidad transaccional por caso de uso.

## Technical Acceptance Criteria
- Se validan los escenarios funcionales con tests automatizados (unit/integration según corresponda).
- Se verifica que no existan dependencias prohibidas desde `domain` hacia Spring/Neo4j/web.
- Se verifica persistencia de relaciones esperadas:
  - `User` -> `Trait` (preferencias)
  - `Pet` -> `Shelter` (vínculo de refugio)
  - `User` -> `Pet` (relación `SWIPED` con acción y timestamp)
  - `User` -> `Pet` (vínculo vía `AdoptionRequest`)
- La consulta de compatibilidad simple devuelve score determinístico y reproducible para mismos datos.
