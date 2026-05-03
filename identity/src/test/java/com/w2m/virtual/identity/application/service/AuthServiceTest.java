package com.w2m.virtual.identity.application.service;

import com.w2m.virtual.identity.domain.AuthResult;
import com.w2m.virtual.identity.infrastructure.adapter.output.inmemory.InMemoryUserRepository;
import com.w2m.virtual.identity.infrastructure.jwt.JwtService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceTest {

    private static final String SECRET =
            "test-secret-test-secret-test-secret-test-secret-test-secret-1234";

    private AuthService newService() {
        return new AuthService(new InMemoryUserRepository(),
                new JwtService(SECRET, "w2m-virtual-identity", 60));
    }

    @Test
    void register_OK_returnsTokenAndPersists() {
        AuthService svc = newService();
        AuthResult r = svc.register("Foo@W2M.local", "password123", "Foo");

        assertThat(r.userId()).isNotNull();
        assertThat(r.email()).isEqualTo("foo@w2m.local");
        assertThat(r.name()).isEqualTo("Foo");
        assertThat(r.token()).isNotBlank();
        assertThat(r.expiresAt()).isNotNull();
    }

    @Test
    void register_duplicateEmail_throws() {
        AuthService svc = newService();
        svc.register("foo@w2m.local", "password123", "Foo");
        assertThatThrownBy(() -> svc.register("FOO@W2M.LOCAL", "password123", "Foo2"))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
    }

    @Test
    void login_OK_withRightCredentials() {
        AuthService svc = newService();
        svc.register("foo@w2m.local", "password123", "Foo");
        AuthResult r = svc.login("foo@w2m.local", "password123");
        assertThat(r.token()).isNotBlank();
    }

    @Test
    void login_wrongPassword_throws() {
        AuthService svc = newService();
        svc.register("foo@w2m.local", "password123", "Foo");
        assertThatThrownBy(() -> svc.login("foo@w2m.local", "WRONG"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_unknownEmail_throws() {
        AuthService svc = newService();
        assertThatThrownBy(() -> svc.login("missing@w2m.local", "password123"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void verify_returnsUser_whenTokenValid() {
        AuthService svc = newService();
        AuthResult r = svc.register("foo@w2m.local", "password123", "Foo");
        assertThat(svc.verify(r.token())).isPresent()
                .get()
                .satisfies(u -> assertThat(u.email()).isEqualTo("foo@w2m.local"));
    }
}
