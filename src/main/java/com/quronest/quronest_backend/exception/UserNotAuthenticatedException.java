package com.quronest.quronest_backend.exception;

import org.springframework.http.HttpStatus;

public class UserNotAuthenticatedException extends CustomApiErrorResponseException {
    public UserNotAuthenticatedException(String message) {
        super(HttpStatus.UNAUTHORIZED, "user_not_authenticated", message);
    }

    public UserNotAuthenticatedException() {
        super(HttpStatus.UNAUTHORIZED, "user_not_authenticated", "User not authenticated");
    }
}
