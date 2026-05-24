# Proposal: Chat-Implementation

## Intent
Habilitar un canal de comunicación seguro y directo entre el Adoptante y el Refugio una vez que se ha confirmado el interés mutuo (Match) a través de un `AdoptionRequest` en estado `ACCEPTED`.

## Scope
1.  **Dominio**: 
    - Definición del modelo `Message` (Value Object).
    - Extensión de la lógica de negocio para validar la apertura de chats.
2.  **Puertos**:
    - `SendMessageUseCase`: Para el envío de nuevos mensajes.
    - `GetChatHistoryUseCase`: Para la recuperación paginada del historial.
    - `MessagePersistencePort`: Abstracción de persistencia.
3.  **Infraestructura**:
    - Adaptador Neo4j para el almacenamiento y recuperación de nodos `Message`.
    - Controlador REST `ChatController` con endpoints de envío y consulta.
    - Validación de seguridad basada en roles (Adoptante/Refugio) y pertenencia al chat.

## Approach (Arquitectura Hexagonal)

### 1. Capa de Dominio
Se implementará `Message` como un Java Record para garantizar la inmutabilidad:
```java
public record Message(
    String content,
    String senderId,
    Instant timestamp
) {}
```

### 2. Capa de Aplicación
`ChatService` orquestará la lógica:
- Antes de enviar un mensaje, se verificará que el `AdoptionRequest` esté `ACCEPTED`.
- Se validará que el `senderId` pertenezca a uno de los dos participantes del Match.

### 3. Capa de Infraestructura (Neo4j)
Se utilizará `Neo4jClient` para manejar las relaciones dinámicas y la paginación.
- **Modelo de Grafos**:
  - `(ar:AdoptionRequest)-[:CONTAINS]->(m:Message)`
  - `(u:User)-[:SENT]->(m:Message)`
- **Consulta de Historial (Cypher)**:
```cypher
MATCH (ar:AdoptionRequest {id: $requestId, status: 'ACCEPTED'})
MATCH (ar)-[:FOR]->(p:Pet)-[:LOCATED_IN]->(s:Shelter)
WHERE (u:User {id: $userId})-[:REQUESTED]->(ar) OR s.id = $userId
WITH ar
MATCH (ar)-[:CONTAINS]->(m:Message)
RETURN m.content as content, m.senderId as senderId, m.timestamp as timestamp
ORDER BY m.timestamp DESC
SKIP $skip LIMIT $limit
```

### 4. Capa Web
Endpoints según la especificación:
- `POST /api/adoption-requests/{requestId}/messages`
- `GET /api/adoption-requests/{requestId}/messages?page=0&size=20`

## Rollback Plan
- El sistema de chat es aditivo. En caso de error crítico, se puede deshabilitar el controlador sin afectar el flujo de adopción actual.
- Los nodos `Message` se pueden eliminar en cascada desde los `AdoptionRequest` si fuera necesario limpiar la base de datos.

## Risks
- **Performance**: El crecimiento ilimitado de mensajes en un solo nodo `AdoptionRequest` podría degradar la performance de lectura si no se indexa correctamente el timestamp. Se recomienda crear un índice sobre `Message(timestamp)`.
- **Identidad**: Se asume que el `userId` del refugio coincide con el `shelterId` para la validación de seguridad, o que existe un mapeo directo.
