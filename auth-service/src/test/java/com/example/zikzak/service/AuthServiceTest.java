package com.example.zikzak.service;

import com.example.zikzak.dto.AuthResponse;
import com.example.zikzak.dto.LoginRequest;
import com.example.zikzak.user.Account;
import com.example.zikzak.user.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AccountService accountService;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginShouldReturnAuthResponse() {
        LoginRequest request = new LoginRequest(
                "user13@example.com",
                "password123"
        );

        Account account = new Account();
        account.setEmail("user13@example.com");
        account.setRole(Role.USER);

        when(accountService.loginCheck(request))
                .thenReturn(account);

        when(jwtService.generateToken(account))
                .thenReturn("test-access-token");

        when(refreshTokenService.issue(account))
                .thenReturn("test-refresh-token");

        when(jwtService.getExpirationSeconds())
                .thenReturn(3600L);

        AuthResponse response = authService.login(request);

        assertEquals(
                "test-access-token",
                response.accessToken()
        );

        assertEquals(
                "test-refresh-token",
                response.refreshToken()
        );

        assertEquals(
                "Bearer",
                response.tokenType()
        );

        assertEquals(
                3600L,
                response.expiresIn()
        );

        verify(accountService).loginCheck(request);
        verify(jwtService).generateToken(account);
        verify(refreshTokenService).issue(account);
        verify(jwtService).getExpirationSeconds();
    }
}