# Delta for matchpet

## MODIFIED Requirements

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
