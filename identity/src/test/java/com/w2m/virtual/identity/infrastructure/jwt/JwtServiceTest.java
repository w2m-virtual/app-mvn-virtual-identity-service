package com.w2m.virtual.identity.infrastructure.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-test-secret-test-secret-1234";
    private final JwtService service = new JwtService(SECRET, "w2m-virtual-identity", 60);

    @Test
    void issueAndVerify_returnsClaims() {
        UUID userId = UUID.randomUUID();
        JwtService.Issued issued = service.issue(userId, "u@w2m.local", "User");

        Optional<JwtService.Verified> v = service.verify(issued.token());

        assertThat(v).isPresent();
        assertThat(v.get().userId()).isEqualTo(userId);
        assertThat(v.get().email()).isEqualTo("u@w2m.local");
        assertThat(v.get().name()).isEqualTo("User");
    }

    @Test
    void verify_returnsEmpty_forExpiredToken() throws Exception {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Instant past = Instant.now().minusSeconds(3600);
        String token = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .issuer("w2m-virtual-identity")
                .issuedAt(Date.from(past.minusSeconds(60)))
                .expiration(Date.from(past))
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        assertThat(service.verify(token)).isEmpty();
    }

    @Test
    void verify_returnsEmpty_forWrongSignature() {
        JwtService other = new JwtService(
                "OTHER-secret-OTHER-secret-OTHER-secret-OTHER-secret-OTHER-1234",
                "w2m-virtual-identity", 60);
        String token = other.issue(UUID.randomUUID(), "u@w2m.local", "U").token();

        assertThat(service.verify(token)).isEmpty();
    }

    @Test
    void verify_returnsEmpty_forNullOrBlank() {
        assertThat(service.verify(null)).isEmpty();
        assertThat(service.verify("   ")).isEmpty();
        assertThat(service.verify("not-a-jwt")).isEmpty();
    }
}
