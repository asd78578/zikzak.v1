package com.example.zikzak.messageservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class JwtService {

    private final String secret;

    public JwtService(
            @Value("${security.jwt.secret}") String secret
    ) {
        this.secret = secret;
    }

    public Long extractUserId(String token) {
        Number userId = parseClaims(token)
                .get("userId", Number.class);

        if (userId == null) {
            throw new IllegalArgumentException(
                    "JWT does not contain userId"
            );
        }

        return userId.longValue();
    }

    public String extractEmail(String token) {
        String email = parseClaims(token)
                .get("email", String.class);

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT does not contain email"
            );
        }

        return email;
    }

    public String extractRole(String token) {
        String role = parseClaims(token)
                .get("role", String.class);

        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT does not contain role"
            );
        }

        return role;
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }
}
