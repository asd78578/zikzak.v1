package com.example.zikzak.chatservice;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

final class TestJwtFactory {

    private static final String SECRET =
            "0123456789012345678901234567890123456789012345678901234567890123";

    private TestJwtFactory() {
    }

    static String createToken(Long accountId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 3_600_000);

        SecretKey key = Keys.hmacShaKeyFor(
                SECRET.getBytes(StandardCharsets.UTF_8)
        );

        return Jwts.builder()
                .subject("user-" + accountId + "@example.com")
                .claim("userId", accountId)
                .claim("email", "user-" + accountId + "@example.com")
                .claim("role", "USER")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }
}
