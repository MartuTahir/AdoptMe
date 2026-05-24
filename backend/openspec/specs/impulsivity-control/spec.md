# impulsivity-control Specification

## Purpose
Definir las reglas de negocio para el control de la impulsividad del adoptante al interactuar (hacer swipe/like) con las mascotas disponibles.

## Requirements

### Requirement: Control de Impulsividad en Likes
El sistema **MUST** limitar el número de acciones `LIKE` que un usuario puede realizar en un período de tiempo. El sistema **MUST NOT** permitir más de 10 `LIKE` en una ventana deslizante de 1 minuto. Si se supera este límite, la operación de swipe **MUST** fallar con `ImpulsiveBehaviorException` y no persistir el swipe excedente.

#### Scenario: Swipes dentro del límite
- **GIVEN** un usuario con 5 likes registrados en el último minuto
- **WHEN** el usuario realiza un nuevo swipe de tipo `LIKE`
- **THEN** el swipe se procesa y persiste con éxito en el sistema

#### Scenario: Bloqueo por exceso de likes (impulsividad)
- **GIVEN** un usuario con 10 likes registrados en el último minuto
- **WHEN** el usuario intenta realizar un nuevo swipe de tipo `LIKE`
- **THEN** la operación es rechazada con un error de dominio por comportamiento impulsivo
- **AND** el nuevo swipe no queda guardado en la base de datos de grafos

#### Scenario: Swipe DISLIKE no se ve afectado por la impulsividad
- **GIVEN** un usuario con 10 likes registrados en el último minuto
- **WHEN** el usuario realiza un swipe de tipo `DISLIKE`
- **THEN** la operation se procesa y persiste con éxito (el límite de impulsividad solo aplica a `LIKE`)
