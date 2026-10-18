package com.agileflow.api.dto.response;

public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresInMs,
    UserSummaryResponse user
) {
    public static AuthResponse of(String accessToken, long expiresInMs, UserSummaryResponse user) {
        return new AuthResponse(accessToken, "Bearer", expiresInMs, user);
    }
}
