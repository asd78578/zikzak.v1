package com.example.zikzak.userservice;

import com.example.zikzak.userservice.profile.UserProfile;
import com.example.zikzak.userservice.profile.UserProfileRepository;
import com.example.zikzak.userservice.profile.UserProfileService;
import com.example.zikzak.userservice.profile.dto.CreateUserProfileRequest;
import com.example.zikzak.userservice.profile.dto.UpdateUserProfileRequest;
import com.example.zikzak.userservice.profile.exception.UserProfileAlreadyExistsException;
import com.example.zikzak.userservice.profile.exception.UserProfileNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository repository;

    private UserProfileService service;

    @BeforeEach
    void setUp() {
        service = new UserProfileService(repository);
    }

    @Test
    void shouldCreateProfile() {
        CreateUserProfileRequest request = createRequest();

        when(repository.existsByAccountId(101L)).thenReturn(false);
        when(repository.saveAndFlush(any(UserProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(101L, request);

        assertThat(response.accountId()).isEqualTo(101L);
        assertThat(response.firstName()).isEqualTo("Alauddin");
        assertThat(response.displayName()).isEqualTo("Alauddin");

        verify(repository).existsByAccountId(101L);
        verify(repository).saveAndFlush(any(UserProfile.class));
    }

    @Test
    void shouldRejectDuplicateProfile() {
        CreateUserProfileRequest request = createRequest();

        when(repository.existsByAccountId(102L)).thenReturn(true);

        assertThatThrownBy(() -> service.create(102L, request))
                .isInstanceOf(UserProfileAlreadyExistsException.class)
                .hasMessageContaining("102");

        verify(repository, never())
                .saveAndFlush(any(UserProfile.class));
    }

    @Test
    void shouldFindProfileByAccountId() {
        UserProfile profile = createProfile(103L);

        when(repository.findByAccountId(103L))
                .thenReturn(Optional.of(profile));

        var response = service.findByAccountId(103L);

        assertThat(response.accountId()).isEqualTo(103L);
        assertThat(response.firstName()).isEqualTo("Alauddin");
        assertThat(response.lastName()).isEqualTo("Developer");
    }

    @Test
    void shouldThrowWhenProfileNotFound() {
        when(repository.findByAccountId(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByAccountId(999L))
                .isInstanceOf(UserProfileNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void shouldUpdateProfile() {
        UserProfile profile = createProfile(104L);

        UpdateUserProfileRequest request =
                new UpdateUserProfileRequest(
                        "Updated",
                        "User",
                        "Updated Name",
                        "Updated bio",
                        "https://example.com/updated-avatar.jpg"
                );

        when(repository.findByAccountId(104L))
                .thenReturn(Optional.of(profile));

        when(repository.saveAndFlush(profile)).thenReturn(profile);

        var response = service.update(104L, request);

        assertThat(response.accountId()).isEqualTo(104L);
        assertThat(response.firstName()).isEqualTo("Updated");
        assertThat(response.lastName()).isEqualTo("User");
        assertThat(response.displayName()).isEqualTo("Updated Name");
        assertThat(response.bio()).isEqualTo("Updated bio");

        verify(repository).saveAndFlush(profile);
    }

    private CreateUserProfileRequest createRequest() {
        return new CreateUserProfileRequest(
                "Alauddin",
                "Developer",
                "Alauddin",
                "ZikZak profile",
                "https://example.com/avatar.jpg"
        );
    }

    private UserProfile createProfile(Long accountId) {
        UserProfile profile = new UserProfile(accountId);
        profile.setFirstName("Alauddin");
        profile.setLastName("Developer");
        profile.setDisplayName("Alauddin");
        profile.setBio("ZikZak profile");
        profile.setAvatarUrl("https://example.com/avatar.jpg");
        return profile;
    }
}
