package com.quronest.quronest_backend.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

@Getter
@Setter
public class CustomApiErrorResponseException extends ResponseStatusException {
    private String errorType = "unknown";
    private String error = "";

    public CustomApiErrorResponseException(HttpStatus httpStatus, String error_type, String error) {
        super(httpStatus, error_type);
        this.errorType = error_type;
        this.error = error;
    }

    public CustomApiErrorResponseException(String error) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "server_error");
        this.error = error;
    }

    public HttpStatusCode getStatus() {
        return this.getStatusCode();
    }
}
