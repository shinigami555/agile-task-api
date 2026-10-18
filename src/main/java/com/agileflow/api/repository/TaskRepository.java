package com.agileflow.api.repository;

import com.agileflow.api.model.Task;
import com.agileflow.api.model.TaskPriority;
import com.agileflow.api.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.creator LEFT JOIN FETCH t.assignee WHERE t.id = :id")
    Optional<Task> findByIdWithDetails(@Param("id") Long id);

    @EntityGraph(attributePaths = {"creator", "assignee"})
    Page<Task> findByAssigneeId(Long assigneeId, Pageable pageable);

    @EntityGraph(attributePaths = {"creator", "assignee"})
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"creator", "assignee"})
    Page<Task> findByPriority(TaskPriority priority, Pageable pageable);
}
