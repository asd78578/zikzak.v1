package com.example.zikzak.controller;

import com.example.zikzak.dto.AccountResponse;
import com.example.zikzak.dto.AuthResponse;
import com.example.zikzak.dto.LoginRequest;
import com.example.zikzak.dto.RegisterRequest;
import com.example.zikzak.service.AccountService;
import com.example.zikzak.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<AccountResponse> register(
            @RequestBody RegisterRequest request
    ) {
        AccountResponse response =
                accountService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request
    ) {
        String token = authService.login(request);

        AuthResponse response = new AuthResponse(
                token,
                "Bearer",
                3_600_000L
        );

        return ResponseEntity.ok(response);
    }
}