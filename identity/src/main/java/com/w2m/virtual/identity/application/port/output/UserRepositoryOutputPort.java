package com.w2m.virtual.identity.application.port.output;

import com.w2m.virtual.identity.domain.User;

import java.util.Optional;
import java.util.UUID;

/** Port-output del subdominio identity — persistencia de usuarios. */
public interface UserRepositoryOutputPort {

    Optional<User> findByEmail(String email);

    Optional<User> findById(UUID userId);

    User save(User user);

    boolean existsByEmail(String email);
}
