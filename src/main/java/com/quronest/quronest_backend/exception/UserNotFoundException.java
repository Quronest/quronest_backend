package com.quronest.quronest_backend.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends CustomApiErrorResponseException {
    public UserNotFoundException(String message) {
        super(HttpStatus.BAD_REQUEST, "user_not_found", message);
    }

    public UserNotFoundException() {
        super(HttpStatus.BAD_REQUEST, "user_not_found", "User not found.");
    }
}
