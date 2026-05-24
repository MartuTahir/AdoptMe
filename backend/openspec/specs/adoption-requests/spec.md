# Adoption Requests Specification

## Purpose
Definir el ciclo de vida de solicitudes de adopción y la creación de `MATCHED` al aceptar, con autorización y trazabilidad básica.

## Functional Requirements

### Requirement: Crear o vincular AdoptionRequest en PENDING
El sistema **MUST** crear una `AdoptionRequest` en estado `PENDING` cuando un usuario registra un `LIKE` sobre una mascota. El sistema **MUST** vincular la solicitud al `User` y `Pet`. El sistema **MUST** ser idempotente por `(userId, petId)` y devolver la solicitud existente sin duplicar registros.

#### Scenario: Creación exitosa de solicitud
- **GIVEN** un usuario y una mascota existentes sin solicitud previa
- **WHEN** se registra un `LIKE`
- **THEN** se crea una `AdoptionRequest` con estado `PENDING`
- **AND** queda vinculada al usuario y la mascota

#### Scenario: Idempotencia ante doble `LIKE`
- **GIVEN** una `AdoptionRequest` `PENDING` existente para `(userId, petId)`
- **WHEN** se registra un nuevo `LIKE` para el mismo par
- **THEN** se devuelve la misma solicitud
- **AND** no se crean duplicados

### Requirement: Aceptar AdoptionRequest y crear MATCHED
El sistema **MUST** permitir que un refugio dueño de la mascota acepte una `AdoptionRequest` en `PENDING`. El sistema **MUST** cambiar el estado a `ACCEPTED` y **MUST** crear una relación `MATCHED` entre `User` y `Pet`. El sistema **MUST** validar la existencia del `LIKE` asociado y **MUST NOT** crear `MATCHED` duplicados.

#### Scenario: Aceptación autorizada
- **GIVEN** una solicitud `PENDING` con `LIKE` existente
- **AND** el refugio dueño de la mascota
- **WHEN** acepta la solicitud
- **THEN** el estado pasa a `ACCEPTED`
- **AND** se crea `MATCHED` entre usuario y mascota

#### Scenario: Aceptación sin ownership
- **GIVEN** una solicitud `PENDING` válida
- **AND** un refugio que no es dueño de la mascota
- **WHEN** intenta aceptar
- **THEN** la operación falla por autorización

#### Scenario: Aceptación ya procesada
- **GIVEN** una solicitud `ACCEPTED` existente
- **WHEN** se intenta aceptar nuevamente
- **THEN** se devuelve la solicitud existente
- **AND** no se crea un nuevo `MATCHED`

### Requirement: Rechazar AdoptionRequest
El sistema **MUST** permitir que un refugio dueño de la mascota rechace una `AdoptionRequest` en `PENDING`. El sistema **MUST** cambiar el estado a `REJECTED` y **MUST NOT** crear `MATCHED`.

#### Scenario: Rechazo autorizado
- **GIVEN** una solicitud `PENDING` válida
- **AND** el refugio dueño de la mascota
- **WHEN** rechaza la solicitud
- **THEN** el estado pasa a `REJECTED`
- **AND** no se crea `MATCHED`

#### Scenario: Rechazo de solicitud aceptada
- **GIVEN** una solicitud `ACCEPTED` existente
- **WHEN** se intenta rechazar
- **THEN** la operación falla por estado inválido

## Non-Functional Requirements

### Requirement: Cumplimiento de Clean/Hexagonal Architecture
El código **MUST** mantener dependencia unidireccional: `web/infrastructure -> application -> domain`; `domain` **MUST NOT** depender de frameworks ni adaptadores.

### Requirement: Calidad y cobertura de tests
La solución **MUST** incluir tests automatizados para todos los casos de uso descritos. La cobertura mínima **SHOULD** ser:
- Dominio y aplicación: 80% líneas.
- Módulo total: 70% líneas.
