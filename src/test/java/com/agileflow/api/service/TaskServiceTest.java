package com.agileflow.api.service;

import com.agileflow.api.dto.request.TaskCreateRequest;
import com.agileflow.api.dto.response.TaskResponse;
import com.agileflow.api.exception.ResourceNotFoundException;
import com.agileflow.api.model.*;
import com.agileflow.api.repository.TaskRepository;
import com.agileflow.api.repository.UserRepository;
import com.agileflow.api.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private User manager;
    private User developer;
    private User otherUser;
    private Task sampleTask;

    @BeforeEach
    void setUp() {
        manager = User.builder()
            .id(1L)
            .username("manager_anna")
            .email("anna@agileflow.com")
            .role(Role.ROLE_MANAGER)
            .build();

        developer = User.builder()
            .id(2L)
            .username("dev_alex")
            .email("alex@agileflow.com")
            .role(Role.ROLE_DEVELOPER)
            .build();

        otherUser = User.builder()
            .id(3L)
            .username("dev_stranger")
            .email("stranger@agileflow.com")
            .role(Role.ROLE_DEVELOPER)
            .build();

        sampleTask = Task.builder()
            .id(100L)
            .title("Upgrade Core Gateway")
            .description("Update firmware")
            .status(TaskStatus.TODO)
            .priority(TaskPriority.HIGH)
            .dueDate(LocalDate.now().plusDays(3))
            .creator(manager)
            .assignee(developer)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("createTask: should create and return new task")
    void createTask_ShouldReturnCreatedTask() {
        TaskCreateRequest request = new TaskCreateRequest(
            "Upgrade Core Gateway",
            "Update firmware",
            TaskStatus.TODO,
            TaskPriority.HIGH,
            LocalDate.now().plusDays(3),
            2L
        );

        when(userRepository.findByUsername("manager_anna")).thenReturn(Optional.of(manager));
        when(userRepository.findById(2L)).thenReturn(Optional.of(developer));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        TaskResponse response = taskService.createTask(request, "manager_anna");

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.title()).isEqualTo("Upgrade Core Gateway");
        assertThat(response.status()).isEqualTo(TaskStatus.TODO);
        assertThat(response.assignee().username()).isEqualTo("dev_alex");

        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("getTaskById: should return task when found")
    void getTaskById_ShouldReturnTask_WhenFound() {
        when(taskRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(sampleTask));

        TaskResponse response = taskService.getTaskById(100L);

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("Upgrade Core Gateway");
    }

    @Test
    @DisplayName("getTaskById: should throw ResourceNotFoundException when not found")
    void getTaskById_ShouldThrowNotFound_WhenNotExists() {
        when(taskRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Task not found with id: 999");
    }

    @Test
    @DisplayName("updateTaskStatus: should allow assigned developer to advance status")
    void updateTaskStatus_ShouldUpdate_WhenAssigneeCalls() {
        when(taskRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserPrincipal devPrincipal = new UserPrincipal(developer);
        TaskResponse response = taskService.updateTaskStatus(100L, TaskStatus.IN_PROGRESS, devPrincipal);

        assertThat(response.status()).isEqualTo(TaskStatus.IN_PROGRESS);
        verify(taskRepository).save(sampleTask);
    }

    @Test
    @DisplayName("updateTaskStatus: should forbid unassigned third-party developer")
    void updateTaskStatus_ShouldThrowAccessDenied_WhenUnrelatedUserCalls() {
        when(taskRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(sampleTask));

        UserPrincipal strangerPrincipal = new UserPrincipal(otherUser);

        assertThatThrownBy(() -> taskService.updateTaskStatus(100L, TaskStatus.DONE, strangerPrincipal))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("You are not authorized to update status");

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteTask: should delete when manager or admin calls")
    void deleteTask_ShouldDelete_WhenManagerCalls() {
        when(taskRepository.findById(100L)).thenReturn(Optional.of(sampleTask));

        UserPrincipal managerPrincipal = new UserPrincipal(manager);
        taskService.deleteTask(100L, managerPrincipal);

        verify(taskRepository, times(1)).delete(sampleTask);
    }
}
