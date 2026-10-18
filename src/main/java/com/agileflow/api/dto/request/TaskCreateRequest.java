package com.agileflow.api.dto.request;

import com.agileflow.api.model.TaskPriority;
import com.agileflow.api.model.TaskStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskCreateRequest(
    @NotBlank(message = "Task title is required")
    @Size(max = 150, message = "Title cannot exceed 150 characters")
    String title,

    String description,

    TaskStatus status,

    TaskPriority priority,

    @FutureOrPresent(message = "Due date must be today or in the future")
    LocalDate dueDate,

    Long assigneeId
) {}
