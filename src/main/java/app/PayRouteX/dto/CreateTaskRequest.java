package app.PayRouteX.dto;

import jakarta.validation.constraints.NotNull;

public class CreateTaskRequest {

    @NotNull(message="Title is required")
    private String title;

    private String description;

    @NotNull(message="Status is required")
    private String status;

    @NotNull(message="Assignee is required")
    private String assignee;
}
