package com.example.zikzak.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Username is required")
        @Size(
                min = 3,
                max = 50,
                message = "Username must contain from 3 to 50 characters"
        )
        @Pattern(
                regexp = "^[a-zA-Z0-9_]+$",
                message = "Username may contain only letters, numbers and underscore"
        )
        String username,

        @NotBlank(message = "Password is required")
        @Size(
                min = 8,
                max = 72,
                message = "Password must contain from 8 to 72 characters"
        )
        String password
) {}
