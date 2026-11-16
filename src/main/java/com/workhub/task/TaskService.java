package com.workhub.task;

import com.workhub.task.dto.CreateTaskRequest;
import com.workhub.task.dto.TaskResponse;
import com.workhub.task.dto.UpdateTaskRequest;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskService {

    private final TaskRepository repository;
    private final MeterRegistry meterRegistry;

    public TaskService(TaskRepository repository, MeterRegistry meterRegistry) {
        this.repository = repository;
        this.meterRegistry = meterRegistry;
    }

    public TaskResponse create(CreateTaskRequest request) {
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setAssignee(request.assignee());
        task.setProjectName(request.projectName());
        task.setDueDate(request.dueDate());
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        Task saved = repository.save(task);
        meterRegistry.counter("workhub.task.creations", "priority", saved.getPriority().name()).increment();
        return TaskResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public TaskResponse get(Long id) {
        return TaskResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> search(TaskStatus status, String assignee, String projectName, Pageable pageable) {
        return repository.search(status, assignee, projectName, pageable).map(TaskResponse::from);
    }

    public TaskResponse update(Long id, UpdateTaskRequest request) {
        Task task = find(id);
        if (task.getStatus().isTerminal()) {
            throw new InvalidStatusTransitionException(task.getStatus(), task.getStatus());
        }
        if (request.title() != null) task.setTitle(request.title());
        if (request.description() != null) task.setDescription(request.description());
        if (request.priority() != null) task.setPriority(request.priority());
        if (request.assignee() != null) task.setAssignee(request.assignee());
        if (request.dueDate() != null) task.setDueDate(request.dueDate());
        return TaskResponse.from(task);
    }

    public TaskResponse changeStatus(Long id, TaskStatus target) {
        Task task = find(id);
        TaskStatus current = task.getStatus();
        if (!current.canTransitionTo(target)) {
            throw new InvalidStatusTransitionException(current, target);
        }
        task.setStatus(target);
        meterRegistry.counter("workhub.task.status.changes", "to", target.name()).increment();
        return TaskResponse.from(task);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        repository.deleteById(id);
    }

    private Task find(Long id) {
        return repository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
    }
}
