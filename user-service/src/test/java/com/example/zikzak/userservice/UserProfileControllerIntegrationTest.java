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
import org.springframework.http.HttpHeaders;
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
class UserProfileControllerIntegrationTest
        extends PostgresContainerTest {

    private static final String MY_PROFILE_URL =
            "/api/v1/profiles/me";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserProfileRepository repository;

    @Test
    void shouldCreateMyProfile() throws Exception {
        CreateUserProfileRequest request = createRequest();

        mockMvc.perform(post(MY_PROFILE_URL)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearerToken(201L)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        ))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        HttpHeaders.LOCATION,
                        MY_PROFILE_URL
                ))
                .andExpect(jsonPath("$.accountId").value(201))
                .andExpect(jsonPath("$.firstName").value("Alauddin"))
                .andExpect(jsonPath("$.displayName").value("Alauddin"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void shouldGetMyProfile() throws Exception {
        repository.saveAndFlush(createProfile(202L));

        mockMvc.perform(get(MY_PROFILE_URL)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearerToken(202L)
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(202))
                .andExpect(jsonPath("$.firstName").value("Alauddin"))
                .andExpect(jsonPath("$.lastName").value("Developer"));
    }

    @Test
    void shouldReturnNotFoundWhenMyProfileDoesNotExist()
            throws Exception {

        mockMvc.perform(get(MY_PROFILE_URL)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearerToken(999999L)
                        ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(
                        "User profile not found for accountId: 999999"
                ))
                .andExpect(jsonPath("$.path").value(MY_PROFILE_URL));
    }

    @Test
    void shouldReturnConflictWhenMyProfileAlreadyExists()
            throws Exception {

        repository.saveAndFlush(createProfile(203L));

        mockMvc.perform(post(MY_PROFILE_URL)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearerToken(203L)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        createRequest()
                                )
                        ))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(
                        "User profile already exists for accountId: 203"
                ));
    }

    @Test
    void shouldUpdateMyProfile() throws Exception {
        repository.saveAndFlush(createProfile(204L));

        UpdateUserProfileRequest request =
                new UpdateUserProfileRequest(
                        "Updated",
                        "User",
                        "Updated Name",
                        "Updated profile",
                        "https://example.com/updated-avatar.jpg"
                );

        mockMvc.perform(put(MY_PROFILE_URL)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearerToken(204L)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(204))
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.displayName")
                        .value("Updated Name"))
                .andExpect(jsonPath("$.bio")
                        .value("Updated profile"));
    }

    @Test
    void shouldReturnBadRequestForInvalidProfile()
            throws Exception {

        CreateUserProfileRequest request =
                new CreateUserProfileRequest(
                        "Alauddin",
                        "Developer",
                        "",
                        null,
                        null
                );

        mockMvc.perform(post(MY_PROFILE_URL)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearerToken(205L)
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        ))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(
                        "Request validation failed"
                ))
                .andExpect(jsonPath(
                        "$.validationErrors.displayName"
                ).value("displayName is required"));
    }

    @Test
    void shouldReturnUnauthorizedWithoutToken()
            throws Exception {

        mockMvc.perform(get(MY_PROFILE_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedForInvalidToken()
            throws Exception {

        mockMvc.perform(get(MY_PROFILE_URL)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer invalid-token"
                        ))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotReturnAnotherUsersProfile()
            throws Exception {

        repository.saveAndFlush(createProfile(301L));

        mockMvc.perform(get(MY_PROFILE_URL)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                bearerToken(302L)
                        ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "User profile not found for accountId: 302"
                ));
    }

    private String bearerToken(Long accountId) {
        return "Bearer " + TestJwtFactory.createToken(accountId);
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
        profile.setAvatarUrl(
                "https://example.com/avatar.jpg"
        );
        return profile;
    }
}
