package com.example.zikzak.controller;

import com.example.zikzak.dto.AccountResponse;
import com.example.zikzak.dto.LoginRequest;
import com.example.zikzak.dto.RegisterRequest;
import com.example.zikzak.service.AccountService;
import com.example.zikzak.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AccountService accountService;
    private final AuthService authService;

    public AuthController(
            AccountService accountService,
            AuthService authService
    ) {
        this.accountService = accountService;
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return accountService.register(request);
    }

    @PostMapping("/login")
    public String login(
            @Valid @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }
}