package com.agileflow.api.dto.response;

import com.agileflow.api.model.Role;
import com.agileflow.api.model.User;

public record UserSummaryResponse(
    Long id,
    String username,
    String email,
    String fullName,
    Role role
) {
    public static UserSummaryResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new UserSummaryResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getRole()
        );
    }
}
