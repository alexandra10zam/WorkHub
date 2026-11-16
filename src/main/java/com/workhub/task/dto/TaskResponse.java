package com.workhub.task.dto;

import com.workhub.task.Task;
import com.workhub.task.TaskPriority;
import com.workhub.task.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        String assignee,
        String projectName,
        LocalDate dueDate,
        Instant createdAt,
        Instant updatedAt
) {
    public static TaskResponse from(Task t) {
        return new TaskResponse(t.getId(), t.getTitle(), t.getDescription(), t.getStatus(),
                t.getPriority(), t.getAssignee(), t.getProjectName(), t.getDueDate(),
                t.getCreatedAt(), t.getUpdatedAt());
    }
}
