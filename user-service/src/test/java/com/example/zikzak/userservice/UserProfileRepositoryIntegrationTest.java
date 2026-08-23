package com.example.zikzak.userservice;

import com.example.zikzak.userservice.profile.UserProfile;
import com.example.zikzak.userservice.profile.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserProfileRepositoryIntegrationTest extends PostgresContainerTest {

    @Autowired
    private UserProfileRepository repository;

    @Test
    void shouldSaveUserProfile() {
        UserProfile profile = createProfile(101L);

        UserProfile saved = repository.saveAndFlush(profile);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAccountId()).isEqualTo(101L);
        assertThat(saved.getDisplayName()).isEqualTo("Alauddin");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldFindProfileByAccountId() {
        repository.saveAndFlush(createProfile(102L));

        var result = repository.findByAccountId(102L);

        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("Alauddin");
        assertThat(repository.existsByAccountId(102L)).isTrue();
    }

    @Test
    void shouldReturnEmptyForUnknownAccountId() {
        var result = repository.findByAccountId(999999L);

        assertThat(result).isEmpty();
        assertThat(repository.existsByAccountId(999999L)).isFalse();
    }

    @Test
    void shouldRejectDuplicateAccountId() {
        repository.saveAndFlush(createProfile(103L));

        UserProfile duplicate = createProfile(103L);

        assertThatThrownBy(() -> repository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private UserProfile createProfile(Long accountId) {
        UserProfile profile = new UserProfile(accountId);
        profile.setFirstName("Alauddin");
        profile.setLastName("Developer");
        profile.setDisplayName("Alauddin");
        profile.setBio("ZikZak user profile");
        profile.setAvatarUrl("https://example.com/avatar.jpg");
        return profile;
    }
}