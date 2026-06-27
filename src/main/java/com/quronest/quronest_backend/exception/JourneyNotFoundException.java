package com.quronest.quronest_backend.exception;

import org.springframework.http.HttpStatus;

public class JourneyNotFoundException extends CustomApiErrorResponseException {
    public JourneyNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "journey_not_found", message);
    }

    public JourneyNotFoundException() {
        super(HttpStatus.NOT_FOUND, "journey_not_found", "User journey not found.");
    }
}
