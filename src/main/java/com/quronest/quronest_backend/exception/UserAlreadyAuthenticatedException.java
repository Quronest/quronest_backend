package com.quronest.quronest_backend.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyAuthenticatedException extends CustomApiErrorResponseException {
    public UserAlreadyAuthenticatedException(String message) {
        super(HttpStatus.BAD_REQUEST, "user_already_authenticated", message);
    }

    public UserAlreadyAuthenticatedException() {
        super(HttpStatus.BAD_REQUEST, "user_already_authenticated", "User already Authenticated.");
    }
}
