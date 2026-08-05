package com.example.zikzak.service;

import com.example.zikzak.user.Account;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final String secret;
    private final long expirationMs;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-ms}") long expirationMs
    ) {
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    public String generateToken(Account account) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(account.getEmail())
                .claim("userId", account.getId())
                .claim("email", account.getEmail())
                .claim("role", account.getRole().name())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(
                        Keys.hmacShaKeyFor(
                                secret.getBytes(StandardCharsets.UTF_8)
                        )
                )
                .compact();
    }
}