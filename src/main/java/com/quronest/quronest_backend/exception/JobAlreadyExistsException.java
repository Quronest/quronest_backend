package com.quronest.quronest_backend.exception;

import org.springframework.http.HttpStatus;

public class JobAlreadyExistsException extends CustomApiErrorResponseException {
    public JobAlreadyExistsException(String message) {
        super(HttpStatus.BAD_REQUEST, "job_already_exists", message);
    }

    public JobAlreadyExistsException() {
        super(HttpStatus.BAD_REQUEST, "job_already_exists", "Job already exists");
    }
}
