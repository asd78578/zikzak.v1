package com.example.zikzak.exception;

public class UsernameAlreadyExistsException extends RuntimeException{

    public UsernameAlreadyExistsException(String message) {
        super("Username already exists: " + message);
    }
}
