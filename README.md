# AdoptMe - MatchPet

Servicio backend para la adopción y compatibilidad de mascotas basado en **Spring Boot 3**, **Java 21** y **Neo4j** (base de datos orientada a grafos). La arquitectura implementada sigue los patrones de **Arquitectura Hexagonal (Puertos y Adaptadores)** para garantizar el desacoplamiento del negocio.

## Requisitos de Entorno
- **Java 21** (JDK 21+)
- **Maven 3.9+**
- **Neo4j 5.x** (Instalación local, Docker o instancia en AuraDB)
- **Docker** (opcional, requerido para ejecutar los tests de integración con Testcontainers)

## Arquitectura y Estructura de Paquetes
El proyecto está estructurado siguiendo principios de Clean Architecture:
- `com.matchpet.domain`: Entidades puras y lógica de dominio sin dependencias externas (`User`, `Pet`, `Trait`, `Shelter`, `CompatibilityEngine`).
- `com.matchpet.application`: Puertos de entrada/salida y servicios de aplicación (`GetCompatibilityUseCase`, `SwipeUseCase`, etc.).
- `com.matchpet.infrastructure`: Adaptadores de persistencia de Neo4j, seguridad (JWT) y controladores web REST.

## Configuración y Ejecución

1. Configura la conexión a tu base de datos Neo4j en `src/main/resources/application.yml`:
   ```yaml
   spring:
     data:
       neo4j:
         uri: bolt://localhost:7687
         username: neo4j
         password: tu_password
   ```

2. Compila el proyecto con Maven:
   ```bash
   mvn clean compile
   ```

3. Ejecuta los tests del proyecto (requiere Docker ejecutándose para Testcontainers):
   ```bash
   mvn test
   ```

4. Inicia la aplicación:
   ```bash
   mvn spring-boot:run
   ```
