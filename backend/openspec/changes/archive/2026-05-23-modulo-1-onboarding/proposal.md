# Proposal: Módulo 1 Onboarding y Formulario de Control

## Intent
Implementar el formulario de control de adopciones para evaluar la idoneidad de los usuarios mediante un score de confianza (`trustScore`) calculado en el dominio y almacenado en el perfil.

## Scope

### In Scope
- Crear Value Object `OnboardingForm` (respuestas a vivienda, tiempo, experiencia, visitas).
- Crear `TrustScoreCalculator` en el dominio con ponderaciones (0 a 100).
- Modificar entidad `User` en Neo4j para incluir la propiedad `trustScore`.
- Crear puerto de entrada `SubmitOnboardingFormUseCase` e implementación.
- Exponer endpoint `POST /api/v1/users/onboarding` en `UserController`.
- Pruebas unitarias (TDD) para el cálculo de confianza e integración para la persistencia del score.

### Out of Scope
- Frontend / Interfaz de usuario.
- Bloqueos automáticos de solicitudes de adopción según el score.

## Capabilities

### New Capabilities
- `onboarding-control`: Proporciona el formulario de control inicial y el cálculo de score de confianza del adoptante.

### Modified Capabilities
- `matchpet-foundation`: Extiende la estructura y persistencia básica de `User` para incluir el score.

## Approach
- Seguir Arquitectura Hexagonal y TDD.
- Lógica de cálculo en un servicio de dominio puro `TrustScoreCalculator` que procese un `OnboardingForm`.
- Persistir el score como atributo numérico en el nodo `@Node User` usando SDN 7.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `src/main/java/com/matchpet/domain/model/User.java` | Modified | Añadir atributo `trustScore`. |
| `src/main/java/com/matchpet/domain/model/OnboardingForm.java` | New | Record con respuestas del formulario. |
| `src/main/java/com/matchpet/domain/service/TrustScoreCalculator.java` | New | Cálculo de confianza del adoptante. |
| `src/main/java/com/matchpet/application/ports/input/SubmitOnboardingFormUseCase.java` | New | Puerto de caso de uso. |
| `src/main/java/com/matchpet/application/services/SubmitOnboardingFormService.java` | New | Implementación del caso de uso. |
| `src/main/java/com/matchpet/infrastructure/adapters/input/web/controllers/UserController.java` | Modified | Endpoint POST para enviar formulario. |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Carga de datos nulos o inválidos | Med | Validación fuerte con `@Valid` y excepciones en el dominio. |

## Rollback Plan
Revertir con `git checkout` los archivos modificados y eliminar los archivos creados en el paquete `com.matchpet`.

## Dependencies
- Neo4j Database + Testcontainers.

## Success Criteria
- [ ] Compilación exitosa y cobertura del nuevo código ≥80%.
- [ ] Persistencia de `trustScore` verificada en Neo4j mediante test de integración.
- [ ] Endpoint `POST /api/v1/users/onboarding` funcionando y protegido por Spring Security.
