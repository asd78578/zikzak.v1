package com.example.zikzak.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
