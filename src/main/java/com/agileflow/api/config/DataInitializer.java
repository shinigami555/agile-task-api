package com.agileflow.api.config;

import com.agileflow.api.model.*;
import com.agileflow.api.repository.TaskRepository;
import com.agileflow.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded. Skipping initial data creation.");
            return;
        }

        log.info("Seeding initial demo data for AgileFlow...");

        // 1. Seed Users
        User admin = User.builder()
            .username("admin")
            .email("admin@agileflow.com")
            .password(passwordEncoder.encode("password123"))
            .fullName("System Administrator")
            .role(Role.ROLE_ADMIN)
            .build();

        User manager = User.builder()
            .username("manager_anna")
            .email("anna@agileflow.com")
            .password(passwordEncoder.encode("password123"))
            .fullName("Anna Lindqvist")
            .role(Role.ROLE_MANAGER)
            .build();

        User devAlex = User.builder()
            .username("dev_alex")
            .email("alex@agileflow.com")
            .password(passwordEncoder.encode("password123"))
            .fullName("Alex Chen")
            .role(Role.ROLE_DEVELOPER)
            .build();

        User devSarah = User.builder()
            .username("dev_sarah")
            .email("sarah@agileflow.com")
            .password(passwordEncoder.encode("password123"))
            .fullName("Sarah Johansson")
            .role(Role.ROLE_DEVELOPER)
            .build();

        userRepository.save(admin);
        userRepository.save(manager);
        userRepository.save(devAlex);
        userRepository.save(devSarah);

        // 2. Seed Agile Sprint Tasks
        Task task1 = Task.builder()
            .title("Implement JWT Security & RBAC Filter")
            .description("Configure stateless authentication filter chain with modern Spring Security 6 lambda DSL.")
            .status(TaskStatus.DONE)
            .priority(TaskPriority.HIGH)
            .dueDate(LocalDate.now().plusDays(2))
            .creator(manager)
            .assignee(devAlex)
            .build();

        Task task2 = Task.builder()
            .title("5G Core Network Function SBA REST API Integration")
            .description("Implement HTTP/2 client calls to query Network Repository Function (NRF) endpoints.")
            .status(TaskStatus.IN_PROGRESS)
            .priority(TaskPriority.CRITICAL)
            .dueDate(LocalDate.now().plusDays(5))
            .creator(manager)
            .assignee(devAlex)
            .build();

        Task task3 = Task.builder()
            .title("Unit & Integration Test Suite using Mockito")
            .description("Achieve >85% code coverage across service and controller layers.")
            .status(TaskStatus.TODO)
            .priority(TaskPriority.MEDIUM)
            .dueDate(LocalDate.now().plusDays(7))
            .creator(admin)
            .assignee(devSarah)
            .build();

        Task task4 = Task.builder()
            .title("Containerize Backend with Docker Multi-Stage Build")
            .description("Build optimized alpine JRE container image and docker-compose manifest with PostgreSQL.")
            .status(TaskStatus.BACKLOG)
            .priority(TaskPriority.LOW)
            .dueDate(LocalDate.now().plusDays(14))
            .creator(manager)
            .assignee(devSarah)
            .build();

        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);
        taskRepository.save(task4);

        log.info("Successfully seeded demo data: 4 users and 4 tasks ready for testing.");
    }
}
