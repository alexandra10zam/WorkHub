package com.workhub.task.dto;

import com.workhub.task.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeStatusRequest(@NotNull(message = "status is required") TaskStatus status) {
}
