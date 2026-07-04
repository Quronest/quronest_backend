package com.quronest.quronest_backend.exception;

import org.springframework.http.HttpStatus;

public class NoteNotFoundException extends CustomApiErrorResponseException {
    public NoteNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "note_not_found", message);
    }

    public NoteNotFoundException() {
        this("Note not found");
    }
}
