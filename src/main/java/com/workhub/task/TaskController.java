package com.workhub.task;

import com.workhub.task.dto.ChangeStatusRequest;
import com.workhub.task.dto.CreateTaskRequest;
import com.workhub.task.dto.TaskResponse;
import com.workhub.task.dto.UpdateTaskRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create a task")
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request) {
        TaskResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a task by id")
    public TaskResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    @Operation(summary = "List tasks, optionally filtered by status, assignee and project")
    public Page<TaskResponse> search(@RequestParam(required = false) TaskStatus status,
                                     @RequestParam(required = false) String assignee,
                                     @RequestParam(required = false) String projectName,
                                     @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
                                     Pageable pageable) {
        return service.search(status, assignee, projectName, pageable);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update a task")
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody UpdateTaskRequest request) {
        return service.update(id, request);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Move a task to a new status (validated against the workflow)")
    public TaskResponse changeStatus(@PathVariable Long id, @Valid @RequestBody ChangeStatusRequest request) {
        return service.changeStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
