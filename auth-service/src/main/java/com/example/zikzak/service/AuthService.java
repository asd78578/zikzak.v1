package com.example.zikzak.service;

import com.example.zikzak.dto.AuthResponse;
import com.example.zikzak.dto.LoginRequest;
import com.example.zikzak.user.Account;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final AccountService accountService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            AccountService accountService,
            JwtService jwtService,
            RefreshTokenService refreshTokenService
    ) {
        this.accountService = accountService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Account account = accountService.loginCheck(request);

        String accessToken = jwtService.generateToken(account);
        String refreshToken = refreshTokenService.issue(account);

        return createResponse(
                accessToken,
                refreshToken
        );
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        RefreshTokenService.RotationResult rotation =
                refreshTokenService.rotate(rawRefreshToken);

        String accessToken =
                jwtService.generateToken(rotation.account());

        return createResponse(
                accessToken,
                rotation.refreshToken()
        );
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revoke(rawRefreshToken);
    }

    private AuthResponse createResponse(
            String accessToken,
            String refreshToken
    ) {
        return new AuthResponse(
                accessToken,
                refreshToken,
                TOKEN_TYPE,
                jwtService.getExpirationSeconds()
        );
    }
}
