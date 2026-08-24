package com.example.zikzak.userservice.profile;

import com.example.zikzak.userservice.profile.dto.CreateUserProfileRequest;
import com.example.zikzak.userservice.profile.dto.UpdateUserProfileRequest;
import com.example.zikzak.userservice.profile.dto.UserProfileResponse;
import com.example.zikzak.userservice.profile.exception.UserProfileAlreadyExistsException;
import com.example.zikzak.userservice.profile.exception.UserProfileNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private final UserProfileRepository repository;

    public UserProfileService(UserProfileRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public UserProfileResponse create(
            Long accountId,
            CreateUserProfileRequest request
    ) {
        if (repository.existsByAccountId(accountId)) {
            throw new UserProfileAlreadyExistsException(accountId);
        }

        UserProfile profile = new UserProfile(accountId);
        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        profile.setDisplayName(request.displayName());
        profile.setBio(request.bio());
        profile.setAvatarUrl(request.avatarUrl());

        UserProfile saved = repository.saveAndFlush(profile);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse findByAccountId(Long accountId) {
        UserProfile profile = findProfile(accountId);
        return toResponse(profile);
    }

    @Transactional
    public UserProfileResponse update(
            Long accountId,
            UpdateUserProfileRequest request
    ) {
        UserProfile profile = findProfile(accountId);

        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        profile.setDisplayName(request.displayName());
        profile.setBio(request.bio());
        profile.setAvatarUrl(request.avatarUrl());

        UserProfile updated = repository.saveAndFlush(profile);
        return toResponse(updated);
    }

    private UserProfile findProfile(Long accountId) {
        return repository.findByAccountId(accountId)
                .orElseThrow(() ->
                        new UserProfileNotFoundException(accountId)
                );
    }

    private UserProfileResponse toResponse(UserProfile profile) {
        return new UserProfileResponse(
                profile.getId(),
                profile.getAccountId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getDisplayName(),
                profile.getBio(),
                profile.getAvatarUrl(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
