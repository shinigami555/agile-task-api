package com.agileflow.api.dto.request;

import com.agileflow.api.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record TaskStatusUpdateRequest(
    @NotNull(message = "New status is required")
    TaskStatus status
) {}
