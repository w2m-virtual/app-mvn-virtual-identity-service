package com.w2m.virtual.identity.infrastructure.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/** Emisión y verificación de JWT HS256. Pure POJO — reutilizable desde el filter del BFF. */
@Component
public class JwtService {

    private final SecretKey key;
    private final String issuer;
    private final long expirationSeconds;

    public JwtService(@Value("${identity.jwt.secret:w2m-virtual-demo-secret-please-change-me-this-must-be-very-long-32+chars}") String secret,
                      @Value("${identity.jwt.issuer:w2m-virtual-identity}") String issuer,
                      @Value("${identity.jwt.expiration-minutes:480}") long expirationMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.expirationSeconds = expirationMinutes * 60L;
    }

    /** Token para {@code userId}; añade claims {@code email} y {@code name}. */
    public Issued issue(UUID userId, String email, String name) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expirationSeconds);
        String token = Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("name", name)
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
        return new Issued(token, exp);
    }

    /** Verifica firma + expiración; devuelve los claims o vacío si KO. */
    public Optional<Verified> verify(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            UUID userId = UUID.fromString(claims.getSubject());
            String email = claims.get("email", String.class);
            String name = claims.get("name", String.class);
            return Optional.of(new Verified(userId, email, name));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public record Issued(String token, Instant expiresAt) {}

    public record Verified(UUID userId, String email, String name) {}
}
