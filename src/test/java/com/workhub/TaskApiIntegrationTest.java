package com.workhub;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** End-to-end: real HTTP, real security, real PostgreSQL schema created by Flyway. */
@Testcontainers
@AutoConfigureObservability
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"workhub.security.username=it", "workhub.security.password=it-secret"})
class TaskApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    TestRestTemplate rest;

    @Test
    void fullTaskLifecycle() {
        TestRestTemplate api = rest.withBasicAuth("it", "it-secret");

        ResponseEntity<JsonNode> created = api.postForEntity("/api/v1/tasks",
                Map.of("title", "Ship v1", "assignee", "alexandra", "projectName", "WorkHub", "priority", "HIGH"),
                JsonNode.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        long id = created.getBody().get("id").asLong();

        assertThat(changeStatus(api, id, "IN_PROGRESS")).isEqualTo(HttpStatus.OK);
        assertThat(changeStatus(api, id, "COMPLETED")).isEqualTo(HttpStatus.CONFLICT);
        assertThat(changeStatus(api, id, "IN_REVIEW")).isEqualTo(HttpStatus.OK);
        assertThat(changeStatus(api, id, "COMPLETED")).isEqualTo(HttpStatus.OK);

        JsonNode page = api.getForObject("/api/v1/tasks?status=COMPLETED&assignee=alexandra", JsonNode.class);
        assertThat(page.get("content")).hasSize(1);
        assertThat(page.get("content").get(0).get("priority").asText()).isEqualTo("HIGH");

        api.delete("/api/v1/tasks/" + id);
        assertThat(api.getForEntity("/api/v1/tasks/" + id, String.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void healthAndMetricsArePublic() {
        ResponseEntity<String> created = rest.withBasicAuth("it", "it-secret").postForEntity("/api/v1/tasks",
                Map.of("title", "Metric check", "assignee", "alexandra", "projectName", "WorkHub"), String.class);
        assertThat(created.getStatusCode()).as(created.getBody()).isEqualTo(HttpStatus.CREATED);

        assertThat(rest.getForEntity("/actuator/health/readiness", String.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(rest.getForObject("/actuator/prometheus", String.class)).contains("workhub_task_creations_total");
    }

    private static HttpStatusCode changeStatus(TestRestTemplate api, long id, String status) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return api.exchange("/api/v1/tasks/" + id + "/status", HttpMethod.PUT,
                new HttpEntity<>(Map.of("status", status), headers), String.class).getStatusCode();
    }
}
