package com.example.zikzak.service;

import com.example.zikzak.component.UserMapper;
import com.example.zikzak.dto.LoginRequest;
import com.example.zikzak.dto.RegisterRequest;
import com.example.zikzak.dto.UserResponse;
import com.example.zikzak.exception.InvalidCredentialsException;
import com.example.zikzak.exception.UserNotFoundException;
import com.example.zikzak.exception.UsernameAlreadyExistsException;
import com.example.zikzak.user.User;
import com.example.zikzak.user.UserRepository;
import org.springframework.stereotype.Service;
import com.example.zikzak.user.Role;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.List;

@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;



    public UserService(UserRepository userRepository
            , UserMapper userMapper, PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;

        this.passwordEncoder = passwordEncoder;

        this.userMapper = userMapper;
    }



    public UserResponse register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    public User loginCheck(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() ->
                        new UserNotFoundException(request.username())
                );

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return user;
    }

}
