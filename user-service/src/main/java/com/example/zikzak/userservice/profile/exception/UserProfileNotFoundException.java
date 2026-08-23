package com.example.zikzak.userservice.profile.exception;

public class UserProfileNotFoundException extends RuntimeException {

    public UserProfileNotFoundException(Long accountId) {
        super("User profile not found for accountId: " + accountId);
    }
}
