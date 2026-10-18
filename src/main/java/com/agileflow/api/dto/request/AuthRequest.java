package com.agileflow.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
    @NotBlank(message = "Username or email is required")
    String usernameOrEmail,

    @NotBlank(message = "Password is required")
    String password
) {}
