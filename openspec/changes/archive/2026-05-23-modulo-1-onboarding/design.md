# Design: Módulo 1 Onboarding y Formulario de Control

## Technical Approach
Implementar una solución desacoplada basada en Arquitectura Hexagonal. Las respuestas del formulario se encapsularán en un Value Object de dominio `OnboardingForm` y se evaluarán usando un servicio de dominio puro `TrustScoreCalculator`. El score de confianza calculado se guardará de forma persistente en el nodo `User` de Neo4j usando SDN 7, permitiendo consultas posteriores optimizadas.

## Architecture Decisions

| Decision | Choices Considered | Selected | Rationale |
| :--- | :--- | :--- | :--- |
| **Cálculo de Score** | Servicio de dominio puro vs Consulta Cypher dinámica | **Servicio de dominio puro** | Mantiene el cálculo matemático desacoplado de la infraestructura de persistencia, facilitando tests unitarios rápidos y cumpliendo con la regla hexagonal. |
| **Uso de Records** | Records vs Clases Java clásicas con Lombok | **Records (Java 21)** | Los records garantizan inmutabilidad nativa para Value Objects y DTOs, simplificando el mantenimiento del estado. |

## Data Flow
El flujo de datos para la evaluación del formulario es:

```
[Web Client] ──(OnboardingRequest)──> [UserController]
                                            │
                                  (OnboardingCommand)
                                            │
                                            ▼
[UserPersistencePort] <─── [SubmitOnboardingFormService] ───> [TrustScoreCalculator]
          │                                 │                              │
     (save/find)                      (UserResult)                  (OnboardingForm)
          │                                 │                              │
          ▼                                 ▼                              ▼
     [Neo4j DB]                      [UserController]               (Trust Score: 0-100)
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `src/main/java/com/matchpet/domain/model/User.java` | Modify | Añadir propiedad `Integer trustScore` y constructor sobrecargado. |
| `src/main/java/com/matchpet/application/ports/input/dto/UserResult.java` | Modify | Añadir propiedad `Integer trustScore` al DTO de salida. |
| `src/main/java/com/matchpet/domain/model/OnboardingForm.java` | Create | Value Object de dominio para las respuestas del adoptante. |
| `src/main/java/com/matchpet/domain/service/TrustScoreCalculator.java` | Create | Servicio de dominio puro para cálculo del score (0 a 100). |
| `src/main/java/com/matchpet/application/ports/input/dto/OnboardingCommand.java` | Create | DTO de entrada para el comando del caso de uso. |
| `src/main/java/com/matchpet/application/ports/input/SubmitOnboardingFormUseCase.java` | Create | Interfaz del puerto de entrada del caso de uso. |
| `src/main/java/com/matchpet/application/services/SubmitOnboardingFormService.java` | Create | Implementación del caso de uso de onboarding. |
| `src/main/java/com/matchpet/infrastructure/adapters/input/web/dto/OnboardingRequest.java` | Create | DTO de entrada REST validado con `jakarta.validation`. |
| `src/main/java/com/matchpet/infrastructure/adapters/input/web/controllers/UserController.java` | Modify | Endpoint `POST /api/users/onboarding`. |

## Interfaces / Contracts

```java
// OnboardingForm (Domain)
public record OnboardingForm(
    String housingType,
    Integer availableHours,
    Boolean hasPreviousExperience,
    Boolean acceptsControlVisits
) {}

// Use Case (Application)
public interface SubmitOnboardingFormUseCase {
    UserResult execute(OnboardingCommand command);
}
```

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| **Unit** | `TrustScoreCalculator` | Pruebas unitarias para validar las ponderaciones (Housing, Hours, Experience, Visits) y la falla de visitas. |
| **Unit** | `SubmitOnboardingFormService` | Mockear `UserPersistencePort` y verificar la orquestación. |
| **Integration** | `UserController` (Web) | `MockMvc` para validar códigos HTTP 200, 400 y validación de campos obligatorios. |
| **Integration** | `UserPersistencePort` (Neo4j) | Integración con Testcontainers Neo4j para asegurar la escritura correcta del `trustScore` en el grafo. |

## Migration / Rollout
No requiere migración de datos. Los nodos existentes en Neo4j recibirán un score por defecto de `0` o `null` si no han realizado el onboarding.

## Open Questions
Ninguna. El alcance está completamente cubierto.
