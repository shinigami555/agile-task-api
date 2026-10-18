package com.agileflow.api.service;

import com.agileflow.api.dto.request.TaskCreateRequest;
import com.agileflow.api.dto.request.TaskUpdateRequest;
import com.agileflow.api.dto.response.TaskResponse;
import com.agileflow.api.exception.BadRequestException;
import com.agileflow.api.exception.ResourceNotFoundException;
import com.agileflow.api.model.Role;
import com.agileflow.api.model.Task;
import com.agileflow.api.model.TaskPriority;
import com.agileflow.api.model.TaskStatus;
import com.agileflow.api.model.User;
import com.agileflow.api.repository.TaskRepository;
import com.agileflow.api.repository.UserRepository;
import com.agileflow.api.security.UserPrincipal;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional
    public TaskResponse createTask(TaskCreateRequest request, String creatorUsername) {
        User creator = userRepository.findByUsername(creatorUsername)
            .orElseThrow(() -> new ResourceNotFoundException("Creator user not found: " + creatorUsername));

        User assignee = null;
        if (request.assigneeId() != null) {
            assignee = userRepository.findById(request.assigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignee user not found with id: " + request.assigneeId()));
        }

        Task task = Task.builder()
            .title(request.title().trim())
            .description(request.description())
            .status(request.status() != null ? request.status() : TaskStatus.TODO)
            .priority(request.priority() != null ? request.priority() : TaskPriority.MEDIUM)
            .dueDate(request.dueDate())
            .creator(creator)
            .assignee(assignee)
            .build();

        Task savedTask = taskRepository.save(task);
        return TaskResponse.fromEntity(savedTask);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return TaskResponse.fromEntity(task);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getAllTasks(
        TaskStatus status,
        TaskPriority priority,
        Long assigneeId,
        String search,
        Pageable pageable
    ) {
        Specification<Task> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }
            if (assigneeId != null) {
                predicates.add(cb.equal(root.get("assignee").get("id"), assigneeId));
            }
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), searchPattern);
                Predicate descLike = cb.like(cb.lower(root.get("description")), searchPattern);
                predicates.add(cb.or(titleLike, descLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return taskRepository.findAll(spec, pageable).map(TaskResponse::fromEntity);
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskUpdateRequest request, UserPrincipal currentUser) {
        Task task = taskRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        validateCanModify(task, currentUser);

        if (request.title() != null && !request.title().trim().isEmpty()) {
            task.setTitle(request.title().trim());
        }
        if (request.description() != null) {
            task.setDescription(request.description());
        }
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        if (request.dueDate() != null) {
            task.setDueDate(request.dueDate());
        }
        if (request.assigneeId() != null) {
            User newAssignee = userRepository.findById(request.assigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignee user not found with id: " + request.assigneeId()));
            task.setAssignee(newAssignee);
        }

        Task updated = taskRepository.save(task);
        return TaskResponse.fromEntity(updated);
    }

    @Transactional
    public TaskResponse updateTaskStatus(Long id, TaskStatus newStatus, UserPrincipal currentUser) {
        if (newStatus == null) {
            throw new BadRequestException("Status cannot be null");
        }

        Task task = taskRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        // Anyone assigned to the task, the task creator, or an ADMIN/MANAGER can advance task status
        boolean isAssignee = task.getAssignee() != null && task.getAssignee().getId().equals(currentUser.getId());
        boolean isCreator = task.getCreator().getId().equals(currentUser.getId());
        boolean isLeadOrAdmin = currentUser.getUser().getRole() == Role.ROLE_ADMIN || currentUser.getUser().getRole() == Role.ROLE_MANAGER;

        if (!isAssignee && !isCreator && !isLeadOrAdmin) {
            throw new AccessDeniedException("You are not authorized to update status of a task not assigned to you");
        }

        task.setStatus(newStatus);
        Task updated = taskRepository.save(task);
        return TaskResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteTask(Long id, UserPrincipal currentUser) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        validateCanModify(task, currentUser);

        taskRepository.delete(task);
    }

    private void validateCanModify(Task task, UserPrincipal currentUser) {
        boolean isCreator = task.getCreator().getId().equals(currentUser.getId());
        boolean isLeadOrAdmin = currentUser.getUser().getRole() == Role.ROLE_ADMIN || currentUser.getUser().getRole() == Role.ROLE_MANAGER;

        if (!isCreator && !isLeadOrAdmin) {
            throw new AccessDeniedException("Only the task creator, a manager, or an administrator can modify or delete this task");
        }
    }
}
