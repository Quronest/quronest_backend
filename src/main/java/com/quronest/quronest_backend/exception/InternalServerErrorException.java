package com.quronest.quronest_backend.exception;

import org.springframework.http.HttpStatus;

public class InternalServerErrorException extends CustomApiErrorResponseException {
    public InternalServerErrorException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "internal_server_error", message);
    }

    public InternalServerErrorException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "internal_server_error", "Internal Server Error.");
    }
}
