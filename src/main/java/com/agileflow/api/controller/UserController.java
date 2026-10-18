package com.agileflow.api.controller;

import com.agileflow.api.dto.response.UserSummaryResponse;
import com.agileflow.api.security.UserPrincipal;
import com.agileflow.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Team directory and user profile management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<UserSummaryResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        UserSummaryResponse response = userService.getProfile(currentUser.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List all team members", description = "Returns all registered users. Useful for task assignment.")
    public ResponseEntity<List<UserSummaryResponse>> getAllUsers() {
        List<UserSummaryResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserSummaryResponse> getUserById(@PathVariable Long id) {
        UserSummaryResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }
}
