package com.quronest.quronest_backend.exception;

import org.springframework.http.HttpStatus;

public class InvalidLimitException extends CustomApiErrorResponseException {
    public InvalidLimitException(String message) {
        super(HttpStatus.BAD_REQUEST, "invalid_limit", message);
    }

    public InvalidLimitException(int min, int max) {
        super(HttpStatus.BAD_REQUEST, "invalid_limit", String.format("Limit must be between %d and %d", min, max));
    }

    public InvalidLimitException() {
        this("Limit must be between 1 and 10");
    }
}
