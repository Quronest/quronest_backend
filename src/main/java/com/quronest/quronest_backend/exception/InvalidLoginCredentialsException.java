package com.quronest.quronest_backend.exception;

import org.springframework.http.HttpStatus;

public class InvalidLoginCredentialsException extends CustomApiErrorResponseException {
    public InvalidLoginCredentialsException(String message) {
        super(HttpStatus.UNAUTHORIZED, "invalid_login_credentials", message);
    }

    public InvalidLoginCredentialsException() {
        super(HttpStatus.UNAUTHORIZED, "invalid_login_credentials", "Invalid Username or Password");
    }
}
