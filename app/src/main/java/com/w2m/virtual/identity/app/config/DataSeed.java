package com.w2m.virtual.identity.app.config;

import com.w2m.virtual.identity.application.port.output.UserRepositoryOutputPort;
import com.w2m.virtual.identity.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Seed determinista de 3 usuarios al arrancar — útil para demo y smoke. */
@Component
public class DataSeed {

    private static final Logger log = LoggerFactory.getLogger(DataSeed.class);

    private final UserRepositoryOutputPort repository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public DataSeed(UserRepositoryOutputPort repository) {
        this.repository = repository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        seedOne("david@w2m.local", "password123", "David Test");
        seedOne("ana@w2m.local", "password123", "Ana Demo");
        seedOne("admin@w2m.local", "password123", "Admin");
        log.info("Identity seed: 3 usuarios cargados (david, ana, admin / password123)");
    }

    private void seedOne(String email, String password, String name) {
        if (repository.existsByEmail(email)) return;
        repository.save(new User(UUID.randomUUID(), email, encoder.encode(password), name));
    }
}
