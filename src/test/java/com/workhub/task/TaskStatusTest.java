package com.workhub.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class TaskStatusTest {

    @Test
    void normalFlowIsAllowed() {
        assertThat(TaskStatus.TODO.canTransitionTo(TaskStatus.IN_PROGRESS)).isTrue();
        assertThat(TaskStatus.IN_PROGRESS.canTransitionTo(TaskStatus.IN_REVIEW)).isTrue();
        assertThat(TaskStatus.IN_REVIEW.canTransitionTo(TaskStatus.COMPLETED)).isTrue();
    }

    @Test
    void cannotSkipReview() {
        assertThat(TaskStatus.IN_PROGRESS.canTransitionTo(TaskStatus.COMPLETED)).isFalse();
        assertThat(TaskStatus.TODO.canTransitionTo(TaskStatus.COMPLETED)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    void terminalStatesHaveNoExits(TaskStatus target) {
        assertThat(TaskStatus.COMPLETED.canTransitionTo(target)).isFalse();
        assertThat(TaskStatus.CANCELLED.canTransitionTo(target)).isFalse();
    }
}
