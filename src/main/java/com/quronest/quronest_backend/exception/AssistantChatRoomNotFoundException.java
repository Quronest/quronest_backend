package com.quronest.quronest_backend.exception;

import org.springframework.http.HttpStatus;

public class AssistantChatRoomNotFoundException extends CustomApiErrorResponseException {
    public AssistantChatRoomNotFoundException(String message) {
        super(HttpStatus.BAD_REQUEST, "chat_room_not_found", message);
    }

    public AssistantChatRoomNotFoundException() {
        this("Chat room not found");
    }
}
