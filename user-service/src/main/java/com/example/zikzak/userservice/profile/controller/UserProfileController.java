package com.example.zikzak.userservice.profile.controller;

import com.example.zikzak.userservice.profile.UserProfileService;
import com.example.zikzak.userservice.profile.dto.CreateUserProfileRequest;
import com.example.zikzak.userservice.profile.dto.UpdateUserProfileRequest;
import com.example.zikzak.userservice.profile.dto.UserProfileResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/profiles")
@Validated
public class UserProfileController {

    private final UserProfileService service;

    public UserProfileController(UserProfileService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UserProfileResponse> create(
            @Valid @RequestBody CreateUserProfileRequest request
    ) {
        UserProfileResponse response = service.create(request);

        URI location = URI.create(
                "/api/v1/profiles/" + response.accountId()
        );

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<UserProfileResponse> findByAccountId(
            @PathVariable
            @Positive(message = "accountId must be positive")
            Long accountId
    ) {
        return ResponseEntity.ok(
                service.findByAccountId(accountId)
        );
    }

    @PutMapping("/{accountId}")
    public ResponseEntity<UserProfileResponse> update(
            @PathVariable
            @Positive(message = "accountId must be positive")
            Long accountId,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        return ResponseEntity.ok(
                service.update(accountId, request)
        );
    }
}
