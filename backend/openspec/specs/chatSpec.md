# Specification: Chat entre Adoptante y Refugio

## Contexto

El servicio de mensajería (chat) se habilita únicamente cuando existe un **Match** confirmado. En términos técnicos, esto ocurre cuando un `AdoptionRequest` alcanza el estado `ACCEPTED`. Este documento detalla cómo extender el sistema actual para soportar esta funcionalidad.

## Requerimientos Funcionales

### 1. Inicialización de Chat
- Un canal de chat se considera "abierto" automáticamente cuando el estado de un `AdoptionRequest` cambia a `ACCEPTED`.
- No se requiere una entidad `Chat` separada inicialmente; el `AdoptionRequest` (en estado ACCEPTED) actúa como el ancla de la conversación.

### 2. Envío de Mensajes
- Solo el Adoptante (dueño del `AdoptionRequest`) y el Refugio (dueño de la `Pet` asociada) pueden enviar mensajes.
- Se debe validar la autorización en cada envío (mismo mecanismo de ownership que en el Módulo 5).
- Un mensaje debe contener:
    - `content`: Texto (máx. 1000 caracteres).
    - `senderId`: ID del usuario que envía.
    - `timestamp`: Momento del envío.

### 3. Recuperación de Mensajes
- Los participantes pueden obtener el historial de mensajes paginado.
- Los mensajes se recuperan asociados al `requestId`.

## Propuesta Técnica

### Modelo de Dominio
- **Message (Value Object/Entity)**: `content`, `senderId`, `timestamp`.
- **AdoptionRequest (Extensión)**: Agregar colección (o referencia) a mensajes.

### Persistencia en Neo4j
- **Nodo**: `Message`.
- **Relaciones**:
    - `(ar:AdoptionRequest)-[:CONTAINS]->(m:Message)`
    - `(u:User)-[:SENT]->(m:Message)`
- **Query de Validación**: 
    - `MATCH (u:User {id: $userId})-[r:REQUESTED|OWNED_BY]-(ar:AdoptionRequest {id: $requestId, status: 'ACCEPTED'})` para verificar si el usuario puede participar en el chat.

### Capa de Aplicación
- **Ports In**: `SendMessageUseCase`, `GetChatHistoryUseCase`.
- **Ports Out**: `MessagePersistencePort`.

### Capa Web
- **Endpoint**: `POST /api/adoption-requests/{requestId}/messages`
- **Endpoint**: `GET /api/adoption-requests/{requestId}/messages?page=0&size=20`

## Siguientes Pasos (Roadmap)
1. **Módulo 6.1**: Implementar `Message` en el dominio y puerto de persistencia.
2. **Módulo 6.2**: Crear adaptador Neo4j para mensajería colgando del nodo `AdoptionRequest`.
3. **Módulo 6.3**: Implementar controladores con seguridad basada en roles (Adoptante vs Refugio).
4. **Módulo 6.4 (Opcional)**: Integrar WebSockets/SSE para notificaciones en tiempo real.
