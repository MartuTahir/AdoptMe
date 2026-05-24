## Exploration: Módulo 1 Onboarding y Score de Confianza

### Current State
El sistema actualmente cuenta con las entidades básicas (`User`, `Pet`, `Shelter`, `Trait`) y un motor de compatibilidad puro (`CompatibilityEngine`) en el dominio que evalúa la compatibilidad basándose en la coincidencia exacta de rasgos (Traits). No existe actualmente ningún mecanismo para evaluar el perfil del adoptante, sus respuestas a un formulario de control, ni una métrica de confianza o idoneidad para la adopción.

### Affected Areas
- `src/main/java/com/matchpet/domain/model/User.java` — Se requerirá agregar el campo de score de confianza (por ejemplo, `Integer trustScore` o `Double trustScore`) y la referencia al estado o respuestas del formulario de control.
- `src/main/java/com/matchpet/domain/model/OnboardingForm.java` [NEW] — Nueva clase de dominio (Value Object/Record) para encapsular las respuestas a las preguntas críticas del formulario de control.
- `src/main/java/com/matchpet/domain/service/TrustScoreCalculator.java` [NEW] — Servicio de dominio puro para calcular el score de confianza en base al formulario de control.
- `src/main/java/com/matchpet/application/ports/in/SubmitOnboardingFormUseCase.java` [NEW] — Interfaz para el caso de uso de onboarding.
- `src/main/java/com/matchpet/infrastructure/adapters/input/web/controllers/UserController.java` — Modificación para exponer el endpoint de carga del formulario y devolución del score.

---

### Approaches

#### 1. Static Rules Engine in Domain (Motor de Reglas Estáticas)
Implementar un servicio de dominio puro (`TrustScoreCalculator`) que reciba un Value Object `OnboardingForm` (que modela las respuestas inmutables) y calcule el score usando ponderaciones y reglas estáticas predefinidas en el código de dominio (ej. Tipo de vivienda, Horas disponibles, Experiencia previa, Aceptación de visitas de control).

- **Pros**:
  - Desacoplado 100% de la infraestructura y de Neo4j.
  - Extremadamente fácil de probar con pruebas unitarias rápidas (TDD puro).
  - Lógica de negocio transparente y determinista.
- **Cons**:
  - Modificar las ponderaciones o agregar preguntas requiere un despliegue de código (a menos que se extraigan parámetros a archivos de configuración tipo `.yml`).
- **Effort**: Low

#### 2. Graph-based Score Calculation (Cálculo Basado en Grafo - Neo4j)
Modelar las preguntas y respuestas directamente como nodos en el grafo de Neo4j (ej. `(User)-[:ANSWERED]->(Option)`). Cada opción tendría un peso asignado como propiedad en la base de datos, y el score se calcularía sumando dinámicamente dichos pesos mediante una consulta Cypher.

- **Pros**:
  - Dinámico: Permite cambiar las ponderaciones y preguntas directamente en la base de datos sin redesplegar código.
- **Cons**:
  - Acopla fuertemente la lógica de cálculo a Neo4j (rompe el principio hexagonal de dominio puro).
  - Pruebas unitarias complejas: requiere simular Neo4j (Testcontainers) incluso para verificar sumas lógicas básicas.
  - Mayor latencia al requerir consultas complejas por cada cálculo de score.
- **Effort**: Medium

#### 3. Decision Tree Domain Model (Modelo de Árbol de Decisión en Dominio)
Representar el cuestionario como una estructura de árbol en memoria en el dominio, donde cada respuesta es evaluada de manera jerárquica o con condicionales dinámicos.

- **Pros**:
  - Altamente flexible y escalable para formularios que cambian dinámicamente según respuestas anteriores.
- **Cons**:
  - Complejidad innecesaria para un formulario de control inicial estándar.
- **Effort**: Medium

---

### Recommendation
Se recomienda el **Approach 1 (Static Rules Engine in Domain)**.
Mantener la lógica del cálculo en un servicio de dominio puro respeta fielmente la arquitectura hexagonal implementada en el módulo de fundación (similar al `CompatibilityEngine`). Se propone crear el Value Object `OnboardingForm` y guardar el score resultante como una propiedad indexada en el nodo `User` para búsquedas eficientes en el grafo. Las ponderaciones básicas sugeridas para el cálculo (escala de 0 a 100) son:
- **Vivienda**: Patio cerrado (+30), Departamento/Sin patio (+15).
- **Tiempo**: Más de 4 horas disponibles (+30), Menos de 4 horas (+10).
- **Experiencia previa**: Sí (+20), No (+10).
- **Aceptación de visitas de control**: Sí (+20), No (Falla automática / Score 0).

---

### Risks
- **Acoplamiento de Seguridad**: El endpoint de envío del formulario debe estar protegido y asegurar que solo el propio usuario autenticado pueda subir sus respuestas.
- **Validaciones de Entrada**: Respuestas incorrectas o nulas deben ser rechazadas a nivel del controlador de Spring Boot usando `jakarta.validation`.

---

### Ready for Proposal
**Yes**. La estrategia técnica está definida y es consistente con los patrones del proyecto. El siguiente paso recomendado es avanzar a la fase de propuestas de diseño y especificación detallada (`sdd-propose` / `sdd-spec`).
