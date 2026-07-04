package com.quronest.quronest_backend.exception;

import org.springframework.http.HttpStatus;

public class JobNotFoundException extends CustomApiErrorResponseException {
    public JobNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "job_not_found", message);
    }

    public JobNotFoundException() {
        this("Job not found.");
    }
}
