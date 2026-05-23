# Agent Onboarding: AdoptMe API (Módulo 5 - Motor de Matches)

Guía rápida de traspaso de contexto para el agente de IA que asume el desarrollo del proyecto AdoptMe.

---

## Quick Path (Paso a Paso Inicial)

Para levantar el contexto de forma inmediata y segura:

1. **Inicializar el Entorno de Trabajo**:
   - Corre `/sdd-init` para que el orquestador identifique la pila tecnológica (Java 21, Spring Boot 3, Neo4j) y configure los estándares del proyecto en el registro de skills.
2. **Archivar Módulos 3 y 4**:
   - Revisa y ejecuta la acción de archivar formalmente el cambio `modulo-3-4-alta-refugio-mascota` antes de continuar, asegurando que los archivos de diseño y specs queden consolidados.
3. **Verificar el Estado Verde**:
   - Ejecuta `mvn test` en tu terminal local para comprobar que la suite completa (incluyendo MockMvc y Testcontainers Neo4j) compila y pasa al 100%.
4. **Comenzar el Módulo 5**:
   - Inicia el nuevo ciclo SDD para el **Módulo 5 (Motor de Matches)** ejecutando `/sdd-new` o redactando la propuesta de diseño y especificaciones correspondiente.

---

## Detalles del Proyecto

| Área | Tecnología / Patrón | Descripción / Configuración |
|------|---------------------|-----------------------------|
| **Core Stack** | Java 21 / Spring Boot 3.4.0 | Uso de Java Records nativos para DTOs y Value Objects de dominio. |
| **Architecture** | Hexagonal Architecture | Separación estricta de responsabilidades: `web/infrastructure -> application -> domain`. El dominio es puro y desacoplado. |
| **Persistencia** | Neo4j (SDN 7) | Base de datos de grafos. Mapeo de relaciones dirigidas `LOCATED_IN` (Pet -> Shelter) y `HAS_TRAIT` (User/Pet -> Trait). |
| **Seguridad** | Spring Security | Autenticación basada en JWT. Los endpoints de mascotas y refugios están restringidos al rol `REFUGIO`. |
| **Pruebas** | JUnit 5, MockMvc, Testcontainers | Testcontainers Neo4j levanta una instancia real de Neo4j en Docker de forma transparente para los tests de integración. |

---

## Estado Actual de los Módulos

- **Módulo 1: Onboarding de Usuarios (Archivado)**
  - Implementado endpoint `POST /api/users/onboarding` para calcular y persistir el `trustScore` del adoptante en Neo4j.
- **Módulo 2: Motor de Impulsividad (Archivado)**
  - Se completó y archivó la persistencia de swipes e impulsividad (likes limitados a 10 por minuto).
- **Módulo 3: Alta de Refugio & Módulo 4: Alta de Mascota (Terminados - Pendiente Archivar)**
  - Diseñamos, implementamos y testeamos el alta de refugio (`POST /api/shelters`) y la validación de refugio existente para el alta de mascotas (`POST /api/pets`).
  - Las ramas ya fueron commiteadas con conventional commits y subidas a GitHub de forma apilada (stacked):
    1. Base: `feat/modulo-3-alta-refugio`
    2. Stacked: `feat/modulo-4-alta-mascota`
  - **Acción requerida**: Archivar formalmente este cambio en la carpeta `openspec/changes/archive/`.

---

## Próximo Paso (Next Steps)

1. **Revisión e Integración de Pull Requests**:
   - Asegurar la aprobación e integración de las dos stacked PRs pusheadas a GitHub.
2. **Archivar Hitos Anteriores**:
   - Completar el ciclo formal de archivo del cambio `modulo-3-4-alta-refugio-mascota`.
3. **Módulo 5: Motor de Matches**:
   - Implementar el ciclo completo cuando un adoptante hace `LIKE` sobre una mascota y el refugio acepta la solicitud (generando el nodo o relación de `MATCH` y habilitando la posterior mensajería). Iniciar este diseño bajo el flujo SDD.
