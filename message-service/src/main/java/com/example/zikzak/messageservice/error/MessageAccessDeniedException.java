package com.example.zikzak.messageservice.error;

import org.springframework.security.access.AccessDeniedException;

public class MessageAccessDeniedException extends AccessDeniedException {

    public MessageAccessDeniedException() {
        super("You cannot modify another user's message");
    }
}
