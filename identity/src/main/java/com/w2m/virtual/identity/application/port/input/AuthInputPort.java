package com.w2m.virtual.identity.application.port.input;

import com.w2m.virtual.identity.domain.AuthResult;
import com.w2m.virtual.identity.domain.User;

import java.util.Optional;

/** Casos de uso del identity service. */
public interface AuthInputPort {

    AuthResult register(String email, String password, String name);

    AuthResult login(String email, String password);

    Optional<User> verify(String token);
}
