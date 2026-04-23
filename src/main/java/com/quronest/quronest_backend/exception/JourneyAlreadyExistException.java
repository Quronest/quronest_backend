package com.quronest.quronest_backend.exception;

import org.springframework.http.HttpStatus;

public class JourneyAlreadyExistException extends CustomApiErrorResponseException {
    public JourneyAlreadyExistException(String message) {
        super(HttpStatus.BAD_REQUEST, "user_journey_exist", message);
    }

    public JourneyAlreadyExistException() {
        super(HttpStatus.BAD_REQUEST, "user_journey_exist", "User journey already started.");
    }

}
