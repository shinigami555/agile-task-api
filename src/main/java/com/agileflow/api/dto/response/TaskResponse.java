package com.agileflow.api.dto.response;

import com.agileflow.api.model.Task;
import com.agileflow.api.model.TaskPriority;
import com.agileflow.api.model.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
    Long id,
    String title,
    String description,
    TaskStatus status,
    TaskPriority priority,
    LocalDate dueDate,
    UserSummaryResponse creator,
    UserSummaryResponse assignee,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static TaskResponse fromEntity(Task task) {
        if (task == null) {
            return null;
        }
        return new TaskResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            task.getPriority(),
            task.getDueDate(),
            UserSummaryResponse.fromEntity(task.getCreator()),
            UserSummaryResponse.fromEntity(task.getAssignee()),
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }
}
