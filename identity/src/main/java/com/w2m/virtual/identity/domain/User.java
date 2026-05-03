package com.w2m.virtual.identity.domain;

import java.util.UUID;

/** Usuario del identity service. {@code passwordHash} es BCrypt. */
public record User(UUID userId, String email, String passwordHash, String name) {
}
