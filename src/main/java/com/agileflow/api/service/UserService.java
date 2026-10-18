package com.agileflow.api.service;

import com.agileflow.api.dto.response.UserSummaryResponse;
import com.agileflow.api.exception.ResourceNotFoundException;
import com.agileflow.api.model.User;
import com.agileflow.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public List<UserSummaryResponse> getAllUsers() {
        return userRepository.findAll().stream()
            .map(UserSummaryResponse::fromEntity)
            .toList();
    }

    public UserSummaryResponse getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return UserSummaryResponse.fromEntity(user);
    }

    public UserSummaryResponse getProfile(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return UserSummaryResponse.fromEntity(user);
    }
}
