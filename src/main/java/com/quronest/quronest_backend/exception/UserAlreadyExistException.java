package com.quronest.quronest_backend.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistException extends CustomApiErrorResponseException {
    public UserAlreadyExistException(String message) {
        super(HttpStatus.BAD_REQUEST, "user_already_exists", message);
    }

    public UserAlreadyExistException() {
        super(HttpStatus.BAD_REQUEST, "user_already_exists", "User already Exists.");
    }
}
