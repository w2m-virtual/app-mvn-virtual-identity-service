package com.w2m.virtual.identity.infrastructure.adapter.input.rest;

import com.w2m.virtual.identity.application.port.input.AuthInputPort;
import com.w2m.virtual.identity.application.service.EmailAlreadyRegisteredException;
import com.w2m.virtual.identity.application.service.InvalidCredentialsException;
import com.w2m.virtual.identity.domain.AuthResult;
import com.w2m.virtual.identity.domain.User;
import com.w2m.virtual.identity.infrastructure.adapter.input.rest.data.AuthDtos.AuthResponse;
import com.w2m.virtual.identity.infrastructure.adapter.input.rest.data.AuthDtos.ErrorResponse;
import com.w2m.virtual.identity.infrastructure.adapter.input.rest.data.AuthDtos.LoginRequest;
import com.w2m.virtual.identity.infrastructure.adapter.input.rest.data.AuthDtos.MeResponse;
import com.w2m.virtual.identity.infrastructure.adapter.input.rest.data.AuthDtos.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/** Endpoints {@code /api/auth/*}. */
@RestController
@RequestMapping("/api/auth")
public class AuthRestAdapter {

    private final AuthInputPort auth;

    public AuthRestAdapter(AuthInputPort auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        AuthResult r = auth.register(req.email(), req.password(), req.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.from(r));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        AuthResult r = auth.login(req.email(), req.password());
        return ResponseEntity.ok(AuthResponse.from(r));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                HttpServletRequest request) {
        Optional<User> u = extractToken(authHeader).flatMap(auth::verify);
        if (u.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("UNAUTHORIZED", "Invalid or missing token"));
        }
        return ResponseEntity.ok(MeResponse.from(u.get()));
    }

    private static Optional<String> extractToken(String header) {
        if (header == null) return Optional.empty();
        String h = header.trim();
        if (h.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return Optional.of(h.substring(7).trim());
        }
        return Optional.empty();
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ErrorResponse> handleDup(EmailAlreadyRegisteredException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("EMAIL_ALREADY_REGISTERED", ex.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCreds(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("INVALID_CREDENTIALS", ex.getMessage()));
    }
}
