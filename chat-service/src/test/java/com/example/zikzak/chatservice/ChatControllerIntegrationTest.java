package com.example.zikzak.chatservice;

import com.example.zikzak.chatservice.chat.ChatMemberRepository;
import com.example.zikzak.chatservice.chat.ChatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ChatControllerIntegrationTest
        extends com.example.zikzak.chatservice.PostgresContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ChatMemberRepository memberRepository;

    @BeforeEach
    void cleanDatabase() {
        memberRepository.deleteAll();
        chatRepository.deleteAll();
    }

    @Test
    void shouldCreateDirectChat() throws Exception {
        String token = com.example.zikzak.chatservice.TestJwtFactory.createToken(101L);

        mockMvc.perform(post("/api/v1/chats")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "participantAccountId": 202
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("DIRECT"))
                .andExpect(jsonPath("$.memberAccountIds[0]").value(101))
                .andExpect(jsonPath("$.memberAccountIds[1]").value(202));

        assertThat(chatRepository.count()).isEqualTo(1);
        assertThat(memberRepository.count()).isEqualTo(2);
    }

    @Test
    void shouldReturnExistingDirectChat() throws Exception {
        String token = com.example.zikzak.chatservice.TestJwtFactory.createToken(301L);
        String body = """
                {
                  "participantAccountId": 302
                }
                """;

        mockMvc.perform(post("/api/v1/chats")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/chats")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        assertThat(chatRepository.count()).isEqualTo(1);
        assertThat(memberRepository.count()).isEqualTo(2);
    }

    @Test
    void shouldReturnOnlyCurrentAccountChats() throws Exception {
        createChat(401L, 402L);
        createChat(403L, 404L);

        String token = com.example.zikzak.chatservice.TestJwtFactory.createToken(401L);

        mockMvc.perform(get("/api/v1/chats")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].memberAccountIds[0]").value(401))
                .andExpect(jsonPath("$[0].memberAccountIds[1]").value(402));
    }

    @Test
    void shouldRejectChatWithSameAccount() throws Exception {
        String token = com.example.zikzak.chatservice.TestJwtFactory.createToken(501L);

        mockMvc.perform(post("/api/v1/chats")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "participantAccountId": 501
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/chats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/chats")
                        .header(
                                "Authorization",
                                "Bearer invalid.jwt.token"
                        ))
                .andExpect(status().isUnauthorized());
    }

    private void createChat(
            Long currentAccountId,
            Long participantAccountId
    ) throws Exception {
        String token =
                com.example.zikzak.chatservice.TestJwtFactory.createToken(currentAccountId);

        mockMvc.perform(post("/api/v1/chats")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "participantAccountId": %d
                                }
                                """.formatted(participantAccountId)))
                .andExpect(status().isCreated());
    }
}
