package com.example.zikzak.messageservice;

import com.example.zikzak.messageservice.chat.ChatMembershipClient;
import com.example.zikzak.messageservice.message.Message;
import com.example.zikzak.messageservice.message.MessageRepository;
import com.example.zikzak.messageservice.message.MessageService;
import com.example.zikzak.messageservice.message.dto.SendMessageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    private static final String TOKEN = "Bearer test-token";

    @Mock
    private MessageRepository repository;

    @Mock
    private ChatMembershipClient chatMembershipClient;

    private MessageService service;

    @BeforeEach
    void setUp() {
        service = new MessageService(
                repository,
                chatMembershipClient
        );
    }

    @Test
    void shouldSendMessageWhenAccountIsChatMember() {
        when(repository.saveAndFlush(any(Message.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.send(
                10L,
                100L,
                TOKEN,
                new SendMessageRequest("  Hello ZikZak  ")
        );

        assertThat(response.chatId()).isEqualTo(10L);
        assertThat(response.senderAccountId()).isEqualTo(100L);
        assertThat(response.content()).isEqualTo("Hello ZikZak");

        verify(chatMembershipClient)
                .verifyMembership(10L, TOKEN);
        verify(repository).saveAndFlush(any(Message.class));
    }

    @Test
    void shouldReturnChatHistory() {
        when(repository.findByChatId(anyLong(), any()))
                .thenReturn(
                        new PageImpl<>(
                                List.of(
                                        new Message(
                                                20L,
                                                101L,
                                                "History message"
                                        )
                                )
                        )
                );

        var history = service.findHistory(
                20L,
                TOKEN,
                0,
                50
        );

        assertThat(history.getContent()).hasSize(1);
        assertThat(history.getContent().getFirst().content())
                .isEqualTo("History message");

        verify(chatMembershipClient)
                .verifyMembership(20L, TOKEN);
    }

    @Test
    void shouldNotSaveMessageWhenMembershipCheckFails() {
        doThrow(
                new AccessDeniedException("Not a chat member")
        ).when(chatMembershipClient)
                .verifyMembership(30L, TOKEN);

        assertThatThrownBy(() ->
                service.send(
                        30L,
                        102L,
                        TOKEN,
                        new SendMessageRequest("Forbidden message")
                )
        ).isInstanceOf(AccessDeniedException.class);

        verify(repository, never())
                .saveAndFlush(any(Message.class));
    }
}