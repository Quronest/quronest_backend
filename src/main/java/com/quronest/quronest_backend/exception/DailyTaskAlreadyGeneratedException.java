package com.quronest.quronest_backend.exception;

import org.springframework.http.HttpStatus;

public class DailyTaskAlreadyGeneratedException extends CustomApiErrorResponseException {
    public DailyTaskAlreadyGeneratedException(String error) {
        super(HttpStatus.BAD_REQUEST, "task_already_generated", error);
    }

    public DailyTaskAlreadyGeneratedException() {
        this("Task already generated.");
    }
}
