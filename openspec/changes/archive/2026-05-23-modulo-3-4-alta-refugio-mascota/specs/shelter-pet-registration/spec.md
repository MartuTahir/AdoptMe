# shelter-pet-registration Specification

## Purpose
Definir los requisitos de comportamiento para el registro de Refugios (Módulo 3) y la vinculación de Mascotas a Refugios existentes (Módulo 4), garantizando la consistencia relacional en Neo4j.

## Requirements

### Requirement: Registro de Refugio
El sistema **MUST** permitir la creación e inserción de un nodo `Shelter` con identificador único, nombre y ubicación. El sistema **MUST** garantizar la unicidad de los ID de los refugios.

#### Scenario: Alta de refugio exitoso
- **GIVEN** datos válidos para un refugio (ID, nombre y ubicación)
- **WHEN** se ejecuta el registro del refugio
- **THEN** el refugio queda persistido en la base de datos Neo4j
- **AND** los datos son accesibles a través de su ID

#### Scenario: Intento de registro con ID duplicado
- **GIVEN** un refugio ya persistido en la base de datos
- **WHEN** se intenta registrar otro refugio con el mismo ID
- **THEN** el sistema rechaza la operación con una excepción de dominio por conflicto de ID
- **AND** no se altera el refugio original

#### Scenario: Datos requeridos faltantes
- **GIVEN** una solicitud de registro con ID, nombre o ubicación vacíos o nulos
- **WHEN** se procesa la solicitud
- **THEN** el sistema rechaza la operación con un error de validación (400 Bad Request)

---

### Requirement: Registro de Mascota con Vínculo Obligatorio a Refugio
El sistema **MUST** permitir registrar una `Pet` asociada a un `Shelter` existente, estableciendo la relación `LOCATED_IN`. El sistema **MUST NOT** permitir la creación de mascotas sin un refugio persistido.

#### Scenario: Registro de mascota en refugio existente
- **GIVEN** un refugio previamente registrado y persistido en el sistema
- **AND** datos válidos de una mascota (ID, nombre y lista de rasgos/traits)
- **WHEN** se registra la mascota asociada al ID del refugio
- **THEN** la mascota queda persistida en Neo4j
- **AND** se crea la relación `LOCATED_IN` dirigida desde el nodo `Pet` hacia el nodo `Shelter`
- **AND** se guardan sus rasgos con la relación `HAS_TRAIT` hacia los nodos `Trait` correspondientes

#### Scenario: Intento de registro en refugio inexistente
- **GIVEN** un ID de refugio que no existe en el sistema
- **WHEN** se intenta registrar una mascota asociada a ese ID de refugio
- **THEN** la operation falla lanzando una excepción de negocio (`EntityNotFoundException` o similar)
- **AND** no se crea ningún nodo `Pet` en Neo4j (integridad relacional garantizada)

#### Scenario: Registro de mascota con ID duplicado
- **GIVEN** una mascota ya registrada en el sistema
- **WHEN** se intenta registrar otra mascota con el mismo ID
- **THEN** el sistema arroja una excepción de conflicto de ID
- **AND** no se crea la nueva mascota
