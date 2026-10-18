package com.agileflow.api.dto.request;

import com.agileflow.api.model.TaskPriority;
import com.agileflow.api.model.TaskStatus;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskUpdateRequest(
    @Size(max = 150, message = "Title cannot exceed 150 characters")
    String title,

    String description,

    TaskStatus status,

    TaskPriority priority,

    LocalDate dueDate,

    Long assigneeId
) {}
