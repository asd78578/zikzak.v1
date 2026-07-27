package com.example.zikzak.service;

import com.example.zikzak.dto.LoginRequest;
import com.example.zikzak.service.JwtService;
import com.example.zikzak.service.UserService;
import com.example.zikzak.user.User;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthService(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    public String login(LoginRequest request) {
        User user = userService.loginCheck(request);
        return jwtService.generateToken(user);
    }
}
