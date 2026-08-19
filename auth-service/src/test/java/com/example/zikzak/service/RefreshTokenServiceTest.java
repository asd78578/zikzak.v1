package com.example.zikzak.service;

import com.example.zikzak.exception.InvalidRefreshTokenException;
import com.example.zikzak.token.RefreshToken;
import com.example.zikzak.token.RefreshTokenRepository;
import com.example.zikzak.user.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(
                refreshTokenRepository,
                604800000L
        );
    }

    @Test
    void shouldIssueRefreshTokenAndStoreOnlyHash() {
        Account account = new Account();

        String rawToken = refreshTokenService.issue(account);

        ArgumentCaptor<RefreshToken> captor =
                ArgumentCaptor.forClass(RefreshToken.class);

        verify(refreshTokenRepository).save(captor.capture());

        RefreshToken savedToken = captor.getValue();

        assertThat(rawToken).isNotBlank();
        assertThat(savedToken.getAccount()).isSameAs(account);
        assertThat(savedToken.getTokenHash())
                .hasSize(64)
                .isNotEqualTo(rawToken);
        assertThat(savedToken.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void shouldRotateValidRefreshToken() {
        Account account = new Account();

        RefreshToken currentToken = new RefreshToken();
        currentToken.setAccount(account);
        currentToken.setTokenHash("old-hash");
        currentToken.setExpiresAt(Instant.now().plusSeconds(3600));

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(currentToken));

        RefreshTokenService.RotationResult result =
                refreshTokenService.rotate("old-raw-token");

        assertThat(currentToken.isRevoked()).isTrue();
        assertThat(result.account()).isSameAs(account);
        assertThat(result.refreshToken()).isNotBlank();

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void shouldRejectUnknownRefreshToken() {
        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                refreshTokenService.rotate("unknown-token")
        ).isInstanceOf(InvalidRefreshTokenException.class);

        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));
    }

    @Test
    void shouldRejectExpiredRefreshToken() {
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setExpiresAt(Instant.now().minusSeconds(1));

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() ->
                refreshTokenService.rotate("expired-token")
        ).isInstanceOf(InvalidRefreshTokenException.class);

        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));
    }

    @Test
    void shouldRevokeExistingRefreshToken() {
        RefreshToken token = new RefreshToken();

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(token));

        refreshTokenService.revoke("raw-token");

        assertThat(token.isRevoked()).isTrue();
    }
}
