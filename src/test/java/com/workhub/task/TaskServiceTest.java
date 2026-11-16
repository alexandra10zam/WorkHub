package com.workhub.task;

import com.workhub.task.dto.CreateTaskRequest;
import com.workhub.task.dto.TaskResponse;
import com.workhub.task.dto.UpdateTaskRequest;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    private TaskRepository repository;
    private SimpleMeterRegistry meters;
    private TaskService service;

    @BeforeEach
    void setUp() {
        repository = mock(TaskRepository.class);
        meters = new SimpleMeterRegistry();
        service = new TaskService(repository, meters);
        when(repository.save(any(Task.class))).thenAnswer(inv -> {
            Task t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });
    }

    @Test
    void createDefaultsToTodoAndMediumAndCountsMetric() {
        TaskResponse created = service.create(
                new CreateTaskRequest("Write API", null, null, "alexandra", "WorkHub", null));

        assertThat(created.id()).isEqualTo(1L);
        assertThat(created.status()).isEqualTo(TaskStatus.TODO);
        assertThat(created.priority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(meters.counter("workhub.task.creations", "priority", "MEDIUM").count()).isEqualTo(1.0);
    }

    @Test
    void getMissingTaskThrows() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(42L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    void changeStatusFollowsWorkflow() {
        Task task = task(TaskStatus.TODO);
        when(repository.findById(1L)).thenReturn(Optional.of(task));

        assertThat(service.changeStatus(1L, TaskStatus.IN_PROGRESS).status()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThatThrownBy(() -> service.changeStatus(1L, TaskStatus.COMPLETED))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void patchOnlyChangesProvidedFields() {
        Task task = task(TaskStatus.IN_PROGRESS);
        when(repository.findById(1L)).thenReturn(Optional.of(task));

        TaskResponse updated = service.update(1L,
                new UpdateTaskRequest(null, null, TaskPriority.HIGH, null, null));

        assertThat(updated.priority()).isEqualTo(TaskPriority.HIGH);
        assertThat(updated.title()).isEqualTo("Original");
    }

    @Test
    void completedTaskIsReadOnly() {
        when(repository.findById(1L)).thenReturn(Optional.of(task(TaskStatus.COMPLETED)));

        assertThatThrownBy(() -> service.update(1L, new UpdateTaskRequest("x", null, null, null, null)))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void deleteMissingTaskThrows() {
        when(repository.existsById(7L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(7L)).isInstanceOf(TaskNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }

    private static Task task(TaskStatus status) {
        Task t = new Task();
        t.setId(1L);
        t.setTitle("Original");
        t.setAssignee("alexandra");
        t.setProjectName("WorkHub");
        t.setStatus(status);
        return t;
    }
}
