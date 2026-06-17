package com.example.zikzak.service;

import com.example.zikzak.component.UserMapper;
import com.example.zikzak.dto.LoginRequest;
import com.example.zikzak.dto.RegisterRequest;
import com.example.zikzak.dto.UserRequest;
import com.example.zikzak.dto.UserResponse;
import com.example.zikzak.exception.UserNotFoundException;
import com.example.zikzak.user.User;
import com.example.zikzak.user.UserRepository;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;

import java.util.Date;
import java.util.List;

@Service
public class UserService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private final String SECRET = "my-secret-key-my-secret-key-my-secret-key";


    public UserService(JwtService jwtService, UserRepository userRepository
            , UserMapper userMapper) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public User create(UserRequest request) {
        User user = new User();
        user.setUsername(request.username());

        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public User update(Long id, UserRequest request) {
        User user = findById(id);
        user.setUsername(request.username());
        return userRepository.save(user);
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }


    public UserResponse register(RegisterRequest request) {

        User user = new User();
        user.setUsername(request.username());

        // пока без bcrypt (добавим позже)
        user.setPassword(request.password());

        return userMapper.toResponse(userRepository.save(user));
    }

    public User loginCheck(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UserNotFoundException(request.username()));

        if (!user.getPassword().equals(request.password())) {
            throw new RuntimeException("Wrong password");
        }

        return user;
    }

    public String login(LoginRequest request) {

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getPassword().equals(request.password())) {
            throw new RuntimeException("Wrong password");
        }

        return jwtService.generateToken(user);
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(getSigningKey())
                .compact();
    }
}
