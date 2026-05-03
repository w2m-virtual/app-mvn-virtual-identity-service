package com.w2m.virtual.identity.infrastructure.adapter.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.w2m.virtual.identity.application.service.AuthService;
import com.w2m.virtual.identity.domain.AuthResult;
import com.w2m.virtual.identity.infrastructure.adapter.output.inmemory.InMemoryUserRepository;
import com.w2m.virtual.identity.infrastructure.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthRestAdapterTest {

    private MockMvc mvc;
    private AuthService svc;
    private final ObjectMapper json = new ObjectMapper();

    @BeforeEach
    void setUp() {
        svc = new AuthService(new InMemoryUserRepository(),
                new JwtService(
                        "test-secret-test-secret-test-secret-test-secret-test-secret-1234",
                        "w2m-virtual-identity", 60));
        mvc = MockMvcBuilders.standaloneSetup(new AuthRestAdapter(svc)).build();
    }

    @Test
    void register_returns201_withToken() throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "email", "foo@w2m.local",
                                "password", "password123",
                                "name", "Foo"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("foo@w2m.local"));
    }

    @Test
    void register_duplicate_returns409() throws Exception {
        svc.register("foo@w2m.local", "password123", "Foo");
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "email", "foo@w2m.local",
                                "password", "password123",
                                "name", "Foo"))))
                .andExpect(status().isConflict());
    }

    @Test
    void login_OK() throws Exception {
        svc.register("foo@w2m.local", "password123", "Foo");
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "email", "foo@w2m.local",
                                "password", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        svc.register("foo@w2m.local", "password123", "Foo");
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "email", "foo@w2m.local",
                                "password", "WRONG"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_withValidToken_returns200() throws Exception {
        AuthResult r = svc.register("foo@w2m.local", "password123", "Foo");
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + r.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("foo@w2m.local"));
    }

    @Test
    void me_withoutToken_returns401() throws Exception {
        mvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_withBadToken_returns401() throws Exception {
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }
}
