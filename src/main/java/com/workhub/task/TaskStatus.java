package com.workhub.task;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Task lifecycle. Allowed transitions are explicit so the API can reject
 * invalid moves, e.g. re-opening a COMPLETED task.
 */
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    ON_HOLD,
    IN_REVIEW,
    COMPLETED,
    CANCELLED;

    private static final Map<TaskStatus, Set<TaskStatus>> TRANSITIONS = Map.of(
            TODO, EnumSet.of(IN_PROGRESS, CANCELLED),
            IN_PROGRESS, EnumSet.of(ON_HOLD, IN_REVIEW, CANCELLED),
            ON_HOLD, EnumSet.of(IN_PROGRESS, CANCELLED),
            IN_REVIEW, EnumSet.of(IN_PROGRESS, COMPLETED),
            COMPLETED, EnumSet.noneOf(TaskStatus.class),
            CANCELLED, EnumSet.noneOf(TaskStatus.class)
    );

    public boolean canTransitionTo(TaskStatus target) {
        return TRANSITIONS.get(this).contains(target);
    }

    public boolean isTerminal() {
        return TRANSITIONS.get(this).isEmpty();
    }
}
