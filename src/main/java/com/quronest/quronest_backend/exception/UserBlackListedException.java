package com.quronest.quronest_backend.exception;

import org.springframework.http.HttpStatus;

public class UserBlackListedException extends CustomApiErrorResponseException {
    public UserBlackListedException(String message) {
        super(HttpStatus.BAD_REQUEST, "user_black_listed", message);
    }

    public UserBlackListedException() {
        super(HttpStatus.BAD_REQUEST, "user_black_listed", "User black listed");
    }
}
