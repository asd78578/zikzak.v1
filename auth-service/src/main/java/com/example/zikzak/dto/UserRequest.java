package com.example.zikzak.dto;

import jakarta.validation.constraints.NotBlank;

public record UserRequest(
        @NotBlank
        String username
) {}
