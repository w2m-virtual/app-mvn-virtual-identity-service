# app-mvn-virtual-identity-service

Identity service del W2M Virtual demo — JWT HS256 + register/login/me en memoria.

## Endpoints

- `POST /api/auth/register` → `{userId, email, name, token, expiresAt}` (201) o `409` si email duplicado.
- `POST /api/auth/login` → idem, o `401` si credenciales mal.
- `GET /api/auth/me` con `Authorization: Bearer <jwt>` → `{userId, email, name}` o `401`.

## Seeds

Tres usuarios precargados:

- `david@w2m.local` / `password123` / "David Test"
- `ana@w2m.local` / `password123` / "Ana Demo"
- `admin@w2m.local` / `password123` / "Admin"

## Config

- `identity.jwt.secret` (default secreto demo, cambiar en prod) — env `JWT_SECRET`.
- `identity.jwt.issuer` — `w2m-virtual-identity`.
- `identity.jwt.expiration-minutes` — 480 (8h).
- Puerto: 8088. CORS para 4200, 8080, 8081.

## Build & run

```bash
mvn -DskipTests package
java -jar app/target/app-0.1.0-SNAPSHOT.jar
```
