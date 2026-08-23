package com.example.zikzak.userservice.profile.dto;

import java.time.Instant;

public record UserProfileResponse(
        Long id,
        Long accountId,
        String firstName,
        String lastName,
        String displayName,
        String bio,
        String avatarUrl,
        Instant createdAt,
        Instant updatedAt
) {
}
