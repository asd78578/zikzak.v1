package com.example.zikzak.userservice;

import com.example.zikzak.userservice.profile.UserProfile;
import com.example.zikzak.userservice.profile.UserProfileRepository;
import com.example.zikzak.userservice.profile.dto.CreateUserProfileRequest;
import com.example.zikzak.userservice.profile.dto.UpdateUserProfileRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserProfileControllerIntegrationTest extends com.example.zikzak.userservice.PostgresContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserProfileRepository repository;

    @Test
    void shouldCreateProfile() throws Exception {
        CreateUserProfileRequest request = createRequest(201L);

        mockMvc.perform(post("/api/v1/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/api/v1/profiles/201"
                ))
                .andExpect(jsonPath("$.accountId").value(201))
                .andExpect(jsonPath("$.firstName").value("Alauddin"))
                .andExpect(jsonPath("$.displayName").value("Alauddin"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void shouldGetProfileByAccountId() throws Exception {
        repository.saveAndFlush(createProfile(202L));

        mockMvc.perform(get("/api/v1/profiles/202"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(202))
                .andExpect(jsonPath("$.firstName").value("Alauddin"))
                .andExpect(jsonPath("$.lastName").value("Developer"));
    }

    @Test
    void shouldReturnNotFoundForUnknownProfile() throws Exception {
        mockMvc.perform(get("/api/v1/profiles/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("User profile not found for accountId: 999999"))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/profiles/999999"));
    }

    @Test
    void shouldReturnConflictForDuplicateProfile() throws Exception {
        repository.saveAndFlush(createProfile(203L));

        CreateUserProfileRequest request = createRequest(203L);

        mockMvc.perform(post("/api/v1/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("User profile already exists for accountId: 203"));
    }

    @Test
    void shouldUpdateProfile() throws Exception {
        repository.saveAndFlush(createProfile(204L));

        UpdateUserProfileRequest request =
                new UpdateUserProfileRequest(
                        "Updated",
                        "User",
                        "Updated Name",
                        "Updated profile",
                        "https://example.com/updated-avatar.jpg"
                );

        mockMvc.perform(put("/api/v1/profiles/204")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(204))
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.displayName").value("Updated Name"))
                .andExpect(jsonPath("$.bio").value("Updated profile"));
    }

    @Test
    void shouldReturnBadRequestForInvalidProfile() throws Exception {
        CreateUserProfileRequest request =
                new CreateUserProfileRequest(
                        -1L,
                        "Alauddin",
                        "Developer",
                        "",
                        null,
                        null
                );

        mockMvc.perform(post("/api/v1/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Request validation failed"))
                .andExpect(jsonPath("$.validationErrors.accountId")
                        .value("accountId must be positive"))
                .andExpect(jsonPath("$.validationErrors.displayName")
                        .value("displayName is required"));
    }

    private CreateUserProfileRequest createRequest(Long accountId) {
        return new CreateUserProfileRequest(
                accountId,
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
