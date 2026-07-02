package com.quronest.quronest_backend.exception;

import org.springframework.http.HttpStatus;

public class DailyTaskNotFoundException extends CustomApiErrorResponseException {
    public DailyTaskNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "task_not_found", message);
    }

    public DailyTaskNotFoundException() {
        this("Task not found.");
    }
}
