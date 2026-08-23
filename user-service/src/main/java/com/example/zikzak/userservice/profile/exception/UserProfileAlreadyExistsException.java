package com.example.zikzak.userservice.profile.exception;

public class UserProfileAlreadyExistsException extends RuntimeException {

    public UserProfileAlreadyExistsException(Long accountId) {
        super("User profile already exists for accountId: " + accountId);
    }
}
