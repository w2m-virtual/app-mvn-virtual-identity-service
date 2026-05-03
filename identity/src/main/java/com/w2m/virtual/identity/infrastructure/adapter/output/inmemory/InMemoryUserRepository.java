package com.w2m.virtual.identity.infrastructure.adapter.output.inmemory;

import com.w2m.virtual.identity.application.port.output.UserRepositoryOutputPort;
import com.w2m.virtual.identity.domain.User;
import org.springframework.stereotype.Repository;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory store key-by-userId con índice secundario por email (lower-case). */
@Repository
public class InMemoryUserRepository implements UserRepositoryOutputPort {

    private final ConcurrentHashMap<UUID, User> byId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, UUID> emailIndex = new ConcurrentHashMap<>();

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        UUID id = emailIndex.get(email.toLowerCase(Locale.ROOT));
        return id == null ? Optional.empty() : Optional.ofNullable(byId.get(id));
    }

    @Override
    public Optional<User> findById(UUID userId) {
        return Optional.ofNullable(byId.get(userId));
    }

    @Override
    public User save(User user) {
        byId.put(user.userId(), user);
        emailIndex.put(user.email().toLowerCase(Locale.ROOT), user.userId());
        return user;
    }

    @Override
    public boolean existsByEmail(String email) {
        return email != null && emailIndex.containsKey(email.toLowerCase(Locale.ROOT));
    }
}
