# Delta for MatchPet

## MODIFIED Requirements

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
