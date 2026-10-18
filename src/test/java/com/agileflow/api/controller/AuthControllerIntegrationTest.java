package com.agileflow.api.controller;

import com.agileflow.api.dto.request.AuthRequest;
import com.agileflow.api.dto.request.RegisterRequest;
import com.agileflow.api.model.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/auth/login - should authenticate seeded admin and return JWT")
    void login_ShouldReturnJwt_WhenCredentialsAreValid() throws Exception {
        AuthRequest loginRequest = new AuthRequest("admin", "password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken", notNullValue()))
            .andExpect(jsonPath("$.tokenType", is("Bearer")))
            .andExpect(jsonPath("$.user.username", is("admin")))
            .andExpect(jsonPath("$.user.role", is("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("POST /api/auth/login - should return 401 on incorrect password")
    void login_ShouldReturnUnauthorized_WhenPasswordIsIncorrect() throws Exception {
        AuthRequest loginRequest = new AuthRequest("admin", "wrong_password");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    @DisplayName("POST /api/auth/register - should create new account and return token")
    void register_ShouldCreateUser_WhenValidPayload() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
            "new_developer",
            "new_dev@agileflow.com",
            "securePass123",
            "New Developer",
            Role.ROLE_DEVELOPER
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accessToken", notNullValue()))
            .andExpect(jsonPath("$.user.username", is("new_developer")))
            .andExpect(jsonPath("$.user.email", is("new_dev@agileflow.com")));
    }
}
