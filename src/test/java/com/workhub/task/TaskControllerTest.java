package com.workhub.task;

import com.workhub.config.SecurityConfig;
import com.workhub.task.dto.TaskResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import(SecurityConfig.class)
class TaskControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    TaskService service;

    @Test
    void rejectsAnonymousRequests() throws Exception {
        mvc.perform(get("/api/v1/tasks/1")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void createReturns201WithLocation() throws Exception {
        when(service.create(any())).thenReturn(response(5L, TaskStatus.TODO));

        mvc.perform(post("/api/v1/tasks").contentType(MediaType.APPLICATION_JSON).content("""
                        {"title":"Write API","assignee":"alexandra","projectName":"WorkHub"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/tasks/5"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    @WithMockUser
    void validationErrorsAreReportedPerField() throws Exception {
        mvc.perform(post("/api/v1/tasks").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").value("title is required"))
                .andExpect(jsonPath("$.errors.assignee").exists());
    }

    @Test
    @WithMockUser
    void unknownTaskIs404() throws Exception {
        when(service.get(99L)).thenThrow(new TaskNotFoundException(99L));

        mvc.perform(get("/api/v1/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Task 99 not found"));
    }

    @Test
    @WithMockUser
    void invalidTransitionIs409() throws Exception {
        when(service.changeStatus(eq(1L), eq(TaskStatus.COMPLETED)))
                .thenThrow(new InvalidStatusTransitionException(TaskStatus.TODO, TaskStatus.COMPLETED));

        mvc.perform(put("/api/v1/tasks/1/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isConflict());
    }

    private static TaskResponse response(Long id, TaskStatus status) {
        return new TaskResponse(id, "Write API", null, status, TaskPriority.MEDIUM,
                "alexandra", "WorkHub", null, Instant.now(), null);
    }
}
