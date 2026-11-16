package com.workhub.task.dto;

import com.workhub.task.TaskPriority;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateTaskRequest(
        @NotBlank(message = "title is required") @Size(max = 150) String title,
        String description,
        TaskPriority priority,
        @NotBlank(message = "assignee is required") @Size(max = 100) String assignee,
        @NotBlank(message = "projectName is required") @Size(max = 100) String projectName,
        @FutureOrPresent(message = "dueDate cannot be in the past") LocalDate dueDate
) {
}
