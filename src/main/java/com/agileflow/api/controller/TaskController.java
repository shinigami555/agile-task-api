package com.agileflow.api.controller;

import com.agileflow.api.dto.request.TaskCreateRequest;
import com.agileflow.api.dto.request.TaskStatusUpdateRequest;
import com.agileflow.api.dto.request.TaskUpdateRequest;
import com.agileflow.api.dto.response.TaskResponse;
import com.agileflow.api.model.TaskPriority;
import com.agileflow.api.model.TaskStatus;
import com.agileflow.api.security.UserPrincipal;
import com.agileflow.api.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Agile task lifecycle and workflow management")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "Create a task", description = "Creates a new task. The authenticated user is set as the creator.")
    public ResponseEntity<TaskResponse> createTask(
        @Valid @RequestBody TaskCreateRequest request,
        @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        TaskResponse response = taskService.createTask(request, currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", description = "Retrieves complete details of a task by its ID.")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        TaskResponse response = taskService.getTaskById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List and search tasks", description = "Retrieves a paginated list of tasks with optional filtering by status, priority, assignee, or text search.")
    public ResponseEntity<Page<TaskResponse>> getAllTasks(
        @RequestParam(required = false) TaskStatus status,
        @RequestParam(required = false) TaskPriority priority,
        @RequestParam(required = false) Long assigneeId,
        @RequestParam(required = false) String search,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<TaskResponse> tasks = taskService.getAllTasks(status, priority, assigneeId, search, pageable);
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task details", description = "Updates title, description, due date, priority, or assignee. Allowed for creator, manager, or admin.")
    public ResponseEntity<TaskResponse> updateTask(
        @PathVariable Long id,
        @RequestBody TaskUpdateRequest request,
        @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        TaskResponse response = taskService.updateTask(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update task status (Kanban column move)", description = "Transitions a task between BACKLOG, TODO, IN_PROGRESS, IN_REVIEW, and DONE.")
    public ResponseEntity<TaskResponse> updateTaskStatus(
        @PathVariable Long id,
        @Valid @RequestBody TaskStatusUpdateRequest request,
        @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        TaskResponse response = taskService.updateTaskStatus(id, request.status(), currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task", description = "Deletes a task by ID. Allowed for creator, manager, or admin.")
    public ResponseEntity<Void> deleteTask(
        @PathVariable Long id,
        @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        taskService.deleteTask(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
