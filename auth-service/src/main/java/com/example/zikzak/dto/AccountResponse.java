package com.example.zikzak.dto;

import com.example.zikzak.user.Role;

import java.time.OffsetDateTime;

public record AccountResponse(
        Long id,
        String email,
        Role role,
        OffsetDateTime createdAt
) {
}
