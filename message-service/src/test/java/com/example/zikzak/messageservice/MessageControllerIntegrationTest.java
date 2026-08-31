package com.example.zikzak.messageservice;

import com.example.zikzak.messageservice.chat.ChatMembershipClient;
import com.example.zikzak.messageservice.message.Message;
import com.example.zikzak.messageservice.message.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MessageControllerIntegrationTest
        extends PostgresContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageRepository repository;

    @MockBean
    private ChatMembershipClient chatMembershipClient;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        reset(chatMembershipClient);
    }

    @Test
    void shouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(
                        get("/api/v1/chats/10/messages")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldSendMessage() throws Exception {
        mockMvc.perform(
                        post("/api/v1/chats/10/messages")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearerToken(100L)
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "content": "Hello ZikZak"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.chatId").value(10))
                .andExpect(jsonPath("$.senderAccountId").value(100))
                .andExpect(jsonPath("$.content")
                        .value("Hello ZikZak"))
                .andExpect(jsonPath("$.status")
                        .value("SENT"));
    }

    @Test
    void shouldReturnMessageHistory() throws Exception {
        repository.saveAndFlush(
                new Message(
                        20L,
                        101L,
                        "First message"
                )
        );

        repository.saveAndFlush(
                new Message(
                        20L,
                        102L,
                        "Second message"
                )
        );

        mockMvc.perform(
                        get("/api/v1/chats/20/messages")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearerToken(101L)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.content",
                                hasSize(2)
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.content[0].content"
                        )
                                .value("Second message")
                )
                .andExpect(
                        jsonPath(
                                "$.content[1].content"
                        )
                                .value("First message")
                );
    }

    @Test
    void shouldRejectBlankMessage() throws Exception {
        mockMvc.perform(
                        post("/api/v1/chats/30/messages")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearerToken(103L)
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "content": "   "
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectAccountOutsideChat() throws Exception {
        String authorization = bearerToken(104L);

        doThrow(
                new AccessDeniedException(
                        "Not a chat member"
                )
        )
                .when(chatMembershipClient)
                .verifyMembership(
                        40L,
                        authorization
                );

        mockMvc.perform(
                        post("/api/v1/chats/40/messages")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        authorization
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "content": "Forbidden message"
                                        }
                                        """)
                )
                .andExpect(status().isForbidden())
                .andExpect(
                        jsonPath("$.title")
                                .value("Chat access denied")
                );
    }

    private String bearerToken(Long accountId) {
        return "Bearer "
                + TestJwtFactory.createToken(accountId);
    }
}