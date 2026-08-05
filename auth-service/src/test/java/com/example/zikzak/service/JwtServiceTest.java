package com.example.zikzak.service;


import com.example.zikzak.user.Account;
import com.example.zikzak.user.Role;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class JwtServiceTest {
    private static final String SECRET =
            "test-jwt-secret-key-that-is-at-least-32-bytes-long";

    private static final long ONE_HOUR_MS = 3_600_000L;

    private Account createAccount() {
        Account account = mock(Account.class);

        when(account.getId()).thenReturn(4L);
        when(account.getEmail())
                .thenReturn("user13@example.com");
        when(account.getRole()).thenReturn(Role.USER);

        return account;
    }

    @Test
    void shouldValidateCorrectTokenAndExtractUserId() {
        // Arrange
        JwtService jwtService =
                new JwtService(SECRET, ONE_HOUR_MS);

        Account account = createAccount();

        String token = jwtService.generateToken(account);

        // Act
        boolean valid = jwtService.isTokenValid(token);
        Long userId = jwtService.extractUserId(token);

        // Assert
        assertThat(valid).isTrue();
        assertThat(userId).isEqualTo(4L);
    }

    @Test
    void shouldRejectTokenSignedWithDifferentSecret() {
        // Arrange
        JwtService tokenCreator =
                new JwtService(SECRET, ONE_HOUR_MS);

        String anotherSecret =
                "another-secret-key-that-is-also-at-least-32-bytes";

        JwtService tokenValidator =
                new JwtService(anotherSecret, ONE_HOUR_MS);

        String token =
                tokenCreator.generateToken(createAccount());

        // Act
        boolean valid =
                tokenValidator.isTokenValid(token);

        // Assert
        assertThat(valid).isFalse();
    }

    @Test
    void shouldRejectExpiredToken() {
        // Arrange
        JwtService jwtService =
                new JwtService(SECRET, -1_000L);

        String token =
                jwtService.generateToken(createAccount());

        // Act
        boolean valid =
                jwtService.isTokenValid(token);

        // Assert
        assertThat(valid).isFalse();
    }



}