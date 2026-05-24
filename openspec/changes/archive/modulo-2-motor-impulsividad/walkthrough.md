# Walkthrough: Módulo 2 Motor de Impulsividad

Se ha completado satisfactoriamente el diseño, desarrollo y validación del **Módulo 2: Motor de Impulsividad** siguiendo la metodología TDD y respetando la arquitectura hexagonal limpia del proyecto.

## Cambios Realizados

### Capa de Dominio (Domain Layer)
- **[NEW] [ImpulsiveBehaviorException.java](file:///C:/Users/jmtor/OneDrive/Escritorio/Programming/2026/BDII/AdoptMe/AdoptMe/src/main/java/com/matchpet/domain/exception/ImpulsiveBehaviorException.java)**: Excepción en tiempo de ejecución para bloquear el comportamiento impulsivo del usuario.
- **[NEW] [ImpulsivityEngine.java](file:///C:/Users/jmtor/OneDrive/Escritorio/Programming/2026/BDII/AdoptMe/AdoptMe/src/main/java/com/matchpet/domain/service/ImpulsivityEngine.java)**: Motor de dominio puro para validar el límite de frecuencia (máximo 10 likes por minuto).

### Capa de Aplicación (Application Layer)
- **[MODIFY] [SwipePersistencePort.java](file:///C:/Users/jmtor/OneDrive/Escritorio/Programming/2026/BDII/AdoptMe/AdoptMe/src/main/java/com/matchpet/application/ports/output/SwipePersistencePort.java)**: Se agregó la firma `long countLikesSince(String userId, Instant since)` para consultar swipes en el adaptador de persistencia.
- **[MODIFY] [SwipeService.java](file:///C:/Users/jmtor/OneDrive/Escritorio/Programming/2026/BDII/AdoptMe/AdoptMe/src/main/java/com/matchpet/application/services/SwipeService.java)**: Se integró el `ImpulsivityEngine` para validar los `LIKE` recientes en la base de datos de grafos antes de persistir la acción.

### Capa de Adaptador (Persistence Adapter Layer)
- **[NEW] [SwipeNeo4jRepository.java](file:///C:/Users/jmtor/OneDrive/Escritorio/Programming/2026/BDII/AdoptMe/AdoptMe/src/main/java/com/matchpet/infrastructure/adapters/output/neo4j/repositories/SwipeNeo4jRepository.java)**: Repositorio SDN 7 con consultas Cypher optimizadas para registrar el swipe de forma directa como una relación `SWIPED` y para contar los likes de un usuario desde un timestamp específico.
- **[NEW] [Neo4jSwipeAdapter.java](file:///C:/Users/jmtor/OneDrive/Escritorio/Programming/2026/BDII/AdoptMe/AdoptMe/src/main/java/com/matchpet/infrastructure/adapters/output/neo4j/Neo4jSwipeAdapter.java)**: Adaptador real de persistencia para swipes que implementa `SwipePersistencePort`.

---

## Pruebas y Verificación

### Tests Unitarios
Se escribieron y validaron las siguientes pruebas:
- **[ImpulsivityEngineTest.java](file:///C:/Users/jmtor/OneDrive/Escritorio/Programming/2026/BDII/AdoptMe/AdoptMe/src/test/java/com/matchpet/domain/service/ImpulsivityEngineTest.java)**: Valida que el motor bloquee al alcanzar 10 o más likes y permita valores menores.
- **[SwipeServiceTest.java](file:///C:/Users/jmtor/OneDrive/Escritorio/Programming/2026/BDII/AdoptMe/AdoptMe/src/test/java/com/matchpet/application/services/SwipeServiceTest.java)**: Valida que la capa de aplicación verifique los likes mediante el puerto y lance la excepción de dominio adecuadamente, sin interferir con otras acciones como `DISLIKE`.

### Tests de Integración
- **[Neo4jSwipeAdapterIntegrationTest.java](file:///C:/Users/jmtor/OneDrive/Escritorio/Programming/2026/BDII/AdoptMe/AdoptMe/src/test/java/com/matchpet/infrastructure/Neo4jSwipeAdapterIntegrationTest.java)**: Test en entorno real con Testcontainers Neo4j para validar la correcta creación física de la relación `SWIPED` en el grafo y la exactitud del conteo de relaciones temporales `LIKE` en el minuto deslizante.

---

## Resultados
Se ejecutó la suite de tests unitarios de forma local (`mvn clean test -Dtest=ImpulsivityEngineTest,SwipeServiceTest`), logrando una compilación correcta y el paso del **100% de los tests (8/8 tests exitosos)**.
