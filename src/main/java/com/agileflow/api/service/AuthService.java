package com.agileflow.api.service;

import com.agileflow.api.dto.request.AuthRequest;
import com.agileflow.api.dto.request.RegisterRequest;
import com.agileflow.api.dto.response.AuthResponse;
import com.agileflow.api.dto.response.UserSummaryResponse;
import com.agileflow.api.exception.BadRequestException;
import com.agileflow.api.model.Role;
import com.agileflow.api.model.User;
import com.agileflow.api.repository.UserRepository;
import com.agileflow.api.security.JwtService;
import com.agileflow.api.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BadRequestException("Username '" + request.username() + "' is already taken");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email '" + request.email() + "' is already registered");
        }

        Role assignedRole = request.role() != null ? request.role() : Role.ROLE_DEVELOPER;

        User user = User.builder()
            .username(request.username().trim())
            .email(request.email().trim().toLowerCase())
            .password(passwordEncoder.encode(request.password()))
            .fullName(request.fullName())
            .role(assignedRole)
            .build();

        User savedUser = userRepository.save(user);
        UserPrincipal principal = new UserPrincipal(savedUser);
        String token = jwtService.generateToken(principal, savedUser.getId(), savedUser.getRole().name());

        return AuthResponse.of(token, jwtService.getExpirationMs(), UserSummaryResponse.fromEntity(savedUser));
    }

    public AuthResponse authenticate(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.usernameOrEmail().trim(),
                request.password()
            )
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = principal.getUser();
        String token = jwtService.generateToken(principal, user.getId(), user.getRole().name());

        return AuthResponse.of(token, jwtService.getExpirationMs(), UserSummaryResponse.fromEntity(user));
    }
}
