package com.example.zikzak.service;

import com.example.zikzak.dto.LoginRequest;
import com.example.zikzak.user.Account;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AccountService accountService;
    private final JwtService jwtService;

    public AuthService(
            AccountService accountService,
            JwtService jwtService
    ) {
        this.accountService = accountService;
        this.jwtService = jwtService;
    }

    public String login(LoginRequest request) {
        Account account = accountService.loginCheck(request);
        return jwtService.generateToken(account);
    }
}
