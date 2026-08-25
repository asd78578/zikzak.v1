package com.example.zikzak.chatservice.chat.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidChatParticipantException
        extends RuntimeException {

    public InvalidChatParticipantException() {
        super("A direct chat cannot be created with the same account");
    }
}
