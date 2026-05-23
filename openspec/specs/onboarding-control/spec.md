# onboarding-control Specification

## Purpose
Definir los requisitos de comportamiento para el formulario de control inicial de adopción y el cálculo del score de confianza del adoptante.

## Requirements

### Requirement: Evaluación del Formulario de Control
El sistema **MUST** validar y procesar un `OnboardingForm` enviado por un usuario autenticado, calculando de forma síncrona su score de confianza (escala de 0 a 100) basado en ponderaciones del dominio y persistiendo dicho score en el perfil del usuario.

#### Scenario: Envío de formulario exitoso con puntaje máximo
- **GIVEN** un usuario autenticado
- **AND** respuestas ideales en el formulario: vivienda con patio cerrado, tiempo disponible > 4h, experiencia previa con mascotas, y aceptación de visitas de control
- **WHEN** se envía el formulario de onboarding
- **THEN** el sistema devuelve un score de 100
- **AND** el score de confianza se guarda en el usuario en Neo4j

#### Scenario: Rechazo automático por no aceptar visitas de control
- **GIVEN** un usuario autenticado
- **AND** respuestas en el formulario donde NO se aceptan visitas de control
- **WHEN** se envía el formulario de onboarding
- **THEN** el sistema devuelve un score de 0
- **AND** el score de confianza de 0 queda guardado en el usuario en Neo4j

#### Scenario: Respuestas parciales o nulas
- **GIVEN** un usuario autenticado
- **AND** una solicitud con campos nulos o vacíos en el formulario
- **WHEN** se intenta enviar el formulario de onboarding
- **THEN** el sistema rechaza la operación con error de validación (400 Bad Request)
- **AND** no se actualiza el score en el perfil del usuario
