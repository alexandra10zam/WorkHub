CREATE TABLE tasks (
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(150) NOT NULL,
    description  TEXT,
    status       VARCHAR(30)  NOT NULL,
    priority     VARCHAR(30)  NOT NULL,
    assignee     VARCHAR(100) NOT NULL,
    project_name VARCHAR(100) NOT NULL,
    due_date     DATE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ,
    version      BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_tasks_status   ON tasks (status);
CREATE INDEX idx_tasks_assignee ON tasks (assignee);
CREATE INDEX idx_tasks_project  ON tasks (project_name);
