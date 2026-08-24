package com.example.zikzak.userservice.profile.controller;

import com.example.zikzak.userservice.profile.UserProfileService;
import com.example.zikzak.userservice.profile.dto.CreateUserProfileRequest;
import com.example.zikzak.userservice.profile.dto.UpdateUserProfileRequest;
import com.example.zikzak.userservice.profile.dto.UserProfileResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/profiles")
public class UserProfileController {

    private static final String MY_PROFILE_PATH =
            "/api/v1/profiles/me";

    private final UserProfileService service;

    public UserProfileController(UserProfileService service) {
        this.service = service;
    }

    @PostMapping("/me")
    public ResponseEntity<UserProfileResponse> createMyProfile(
            Authentication authentication,
            @Valid @RequestBody CreateUserProfileRequest request
    ) {
        Long accountId = extractAccountId(authentication);

        UserProfileResponse response =
                service.create(accountId, request);

        return ResponseEntity
                .created(URI.create(MY_PROFILE_PATH))
                .body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            Authentication authentication
    ) {
        Long accountId = extractAccountId(authentication);

        return ResponseEntity.ok(
                service.findByAccountId(accountId)
        );
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        Long accountId = extractAccountId(authentication);

        return ResponseEntity.ok(
                service.update(accountId, request)
        );
    }

    private Long extractAccountId(
            Authentication authentication
    ) {
        return (Long) authentication.getPrincipal();
    }
}
