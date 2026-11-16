package com.workhub.task.dto;

import com.workhub.task.TaskPriority;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Partial update (PATCH): only non-null fields are applied. */
public record UpdateTaskRequest(
        @Size(max = 150) String title,
        String description,
        TaskPriority priority,
        @Size(max = 100) String assignee,
        LocalDate dueDate
) {
}
