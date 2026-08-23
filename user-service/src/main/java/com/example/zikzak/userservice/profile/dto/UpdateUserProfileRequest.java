package com.example.zikzak.userservice.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(

        @Size(max = 100, message = "firstName must not exceed 100 characters")
        String firstName,

        @Size(max = 100, message = "lastName must not exceed 100 characters")
        String lastName,

        @NotBlank(message = "displayName is required")
        @Size(max = 150, message = "displayName must not exceed 150 characters")
        String displayName,

        @Size(max = 500, message = "bio must not exceed 500 characters")
        String bio,

        @Size(max = 2048, message = "avatarUrl must not exceed 2048 characters")
        String avatarUrl
) {
}
