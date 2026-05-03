package com.w2m.virtual.identity.application.service;

import com.w2m.virtual.identity.application.port.input.AuthInputPort;
import com.w2m.virtual.identity.application.port.output.UserRepositoryOutputPort;
import com.w2m.virtual.identity.domain.AuthResult;
import com.w2m.virtual.identity.domain.User;
import com.w2m.virtual.identity.infrastructure.jwt.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/** Orquesta register/login + emisión JWT. */
@Service
public class AuthService implements AuthInputPort {

    private final UserRepositoryOutputPort repository;
    private final JwtService jwt;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserRepositoryOutputPort repository, JwtService jwt) {
        this.repository = repository;
        this.jwt = jwt;
    }

    @Override
    public AuthResult register(String email, String password, String name) {
        String normalized = normalize(email);
        if (repository.existsByEmail(normalized)) {
            throw new EmailAlreadyRegisteredException(normalized);
        }
        User user = new User(UUID.randomUUID(), normalized, encoder.encode(password), name);
        repository.save(user);
        return issue(user);
    }

    @Override
    public AuthResult login(String email, String password) {
        String normalized = normalize(email);
        User user = repository.findByEmail(normalized)
                .orElseThrow(InvalidCredentialsException::new);
        if (!encoder.matches(password, user.passwordHash())) {
            throw new InvalidCredentialsException();
        }
        return issue(user);
    }

    @Override
    public Optional<User> verify(String token) {
        return jwt.verify(token)
                .flatMap(v -> repository.findById(v.userId()));
    }

    private AuthResult issue(User user) {
        JwtService.Issued issued = jwt.issue(user.userId(), user.email(), user.name());
        return new AuthResult(user.userId(), user.email(), user.name(),
                issued.token(), issued.expiresAt());
    }

    private static String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
