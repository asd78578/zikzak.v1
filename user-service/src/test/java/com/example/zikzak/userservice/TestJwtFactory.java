package com.example.zikzak.userservice;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Date;

final class TestJwtFactory {

    static final String TEST_SECRET =
            "test-jwt-secret-key-at-least-32-characters-long";

    private TestJwtFactory() {
    }

    static String createToken(Long accountId) {
        Date now = new Date();
        Date expiration = new Date(
                now.getTime() + 3_600_000
        );

        return Jwts.builder()
                .subject("test@example.com")
                .claim("userId", accountId)
                .claim("email", "test@example.com")
                .claim("role", "USER")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(
                        Keys.hmacShaKeyFor(
                                TEST_SECRET.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        )
                )
                .compact();
    }
}
