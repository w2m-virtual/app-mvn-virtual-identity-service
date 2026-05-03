package com.w2m.virtual.identity.domain;

import java.time.Instant;
import java.util.UUID;

/** Resultado de register/login: identifica al usuario y entrega el JWT firmado. */
public record AuthResult(UUID userId, String email, String name, String token, Instant expiresAt) {
}
