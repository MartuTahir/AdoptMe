# Specification: Implementar Endpoints Faltantes

## Intent
Implementar 2 endpoints faltantes en el backend activo (`src/`) para consumo del frontend:
1. `GET /api/pets` — Listar pets con paginación
2. `GET /api/users/me/matches` — Listar matches del usuario autenticado

## Out of Scope
- `POST /api/swipes/like` y `POST /api/swipes/dislike` como endpoints separados (el unified `POST /api/swipes` se mantiene)
- Chat endpoints (ya existen en `src/`)

---

## Capability: pet-listing

### Requirement
El sistema SHALL permitir a usuarios autenticados listar todos los pets disponibles con paginación.

### API Contract
```
GET /api/pets?page={0}&size={20}
Authorization: Bearer <token>

Response 200:
[
  {
    "id": "pet-1",
    "name": "Luna",
    "shelter": { "id": "shelter-1", "name": "Refugio Esperanza", "location": "Palermo" },
    "traits": [{ "id": "t1", "name": "Juguetón" }]
  }
]

Response 401: Unauthorized (no token)
Response 400: Bad Request (page < 0, size <= 0, size > 100)
```

### Scenarios

**Scenario 1: Authenticated user lists pets**
- Given: Usuario autenticado
- When: `GET /api/pets?page=0&size=20`
- Then: `200 OK` con lista de hasta 20 pets

**Scenario 2: Unauthenticated user**
- Given: Sin token de autenticación
- When: `GET /api/pets`
- Then: `401 Unauthorized`

**Scenario 3: Invalid pagination**
- Given: Usuario autenticado
- When: `GET /api/pets?page=-1`
- Then: `400 Bad Request`

**Scenario 4: Empty pet list**
- Given: Usuario autenticado, no hay pets registrados
- When: `GET /api/pets`
- Then: `200 OK` con `[]`

---

## Capability: user-matches

### Requirement
El sistema SHALL permitir a un usuario autenticado obtener sus adoption requests con status ACCEPTED (matches).

### API Contract
```
GET /api/users/me/matches
Authorization: Bearer <token>

Response 200:
[
  {
    "requestId": "user1@matchpet.com:pet-1",
    "petId": "pet-1",
    "acceptedAt": "2026-05-24T18:00:00Z"
  }
]

Response 401: Unauthorized (no token)
```

### Scenarios

**Scenario 1: User with matches**
- Given: Usuario autenticado con 2 adoption requests ACCEPTED
- When: `GET /api/users/me/matches`
- Then: `200 OK` con 2 matches

**Scenario 2: User without matches**
- Given: Usuario autenticado sin adoption requests ACCEPTED
- When: `GET /api/users/me/matches`
- Then: `200 OK` con `[]`

**Scenario 3: Unauthenticated user**
- Given: Sin token de autenticación
- When: `GET /api/users/me/matches`
- Then: `401 Unauthorized`

---

## Security Changes

### SecurityConfig
- `/api/pets/**` SHALL be split by HTTP method:
  - `GET /api/pets/**` → authenticated (any role)
  - `POST /api/pets/**` → `hasRole('REFUGIO')` (existing)
- `/api/users/me/matches` → authenticated (any role)
