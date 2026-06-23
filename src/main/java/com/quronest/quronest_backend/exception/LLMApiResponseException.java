package com.quronest.quronest_backend.exception;

public class LLMApiResponseException extends RuntimeException {
    public LLMApiResponseException(String message) {
        super(message);
    }
}
