package com.w2m.virtual.identity.infrastructure.adapter.input.rest.data;

import com.w2m.virtual.identity.domain.AuthResult;
import com.w2m.virtual.identity.domain.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class AuthDtos {

    private AuthDtos() {}

    public record RegisterRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6) String password,
            @NotBlank String name) {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {
    }

    public record AuthResponse(UUID userId, String email, String name, String token, Instant expiresAt) {
        public static AuthResponse from(AuthResult r) {
            return new AuthResponse(r.userId(), r.email(), r.name(), r.token(), r.expiresAt());
        }
    }

    public record MeResponse(UUID userId, String email, String name) {
        public static MeResponse from(User u) {
            return new MeResponse(u.userId(), u.email(), u.name());
        }
    }

    public record ErrorResponse(String error, String message) {}
}
