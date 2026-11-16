package com.workhub.task;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(TaskStatus from, TaskStatus to) {
        super("Cannot move task from " + from + " to " + to);
    }
}
