package com.example.zikzak.messageservice;

import com.example.zikzak.messageservice.chat.ChatMembershipClient;
import com.example.zikzak.messageservice.error.MessageAccessDeniedException;
import com.example.zikzak.messageservice.error.MessageNotFoundException;
import com.example.zikzak.messageservice.message.Message;
import com.example.zikzak.messageservice.message.MessageRepository;
import com.example.zikzak.messageservice.message.MessageService;
import com.example.zikzak.messageservice.message.MessageStatus;
import com.example.zikzak.messageservice.message.dto.EditMessageRequest;
import com.example.zikzak.messageservice.message.dto.SendMessageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.example.zikzak.messageservice.message.dto.MessageResponse;
import com.example.zikzak.messageservice.event.MessageEventPublisher;
import com.example.zikzak.messageservice.event.MessageEventType;

import java.util.List;
import java.util.Optional;

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

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private MessageEventPublisher eventPublisher;

    private MessageService service;

    @BeforeEach
    void setUp() {
        service = new MessageService(
                repository,
                chatMembershipClient,
                messagingTemplate,
                eventPublisher
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
        assertThat(response.status()).isEqualTo(MessageStatus.SENT);

        verify(chatMembershipClient)
                .verifyMembership(10L, TOKEN);

        verify(repository)
                .saveAndFlush(any(Message.class));

        verify(messagingTemplate)
                .convertAndSend(
                        eq("/topic/chats/10"),
                        any(MessageResponse.class)
                );

        verify(eventPublisher).publish(
                eq(MessageEventType.MESSAGE_SENT),
                any(Message.class)
        );
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
    void shouldEditOwnMessage() {
        Message message = new Message(
                30L,
                200L,
                "Old content"
        );

        when(repository.findByIdAndChatId(1L, 30L))
                .thenReturn(Optional.of(message));
        when(repository.saveAndFlush(message))
                .thenReturn(message);

        var response = service.edit(
                30L,
                1L,
                200L,
                TOKEN,
                new EditMessageRequest("  New content  ")
        );

        assertThat(response.content()).isEqualTo("New content");
        assertThat(response.status()).isEqualTo(MessageStatus.EDITED);

        verify(chatMembershipClient)
                .verifyMembership(30L, TOKEN);
        verify(repository).saveAndFlush(message);

        verify(eventPublisher).publish(
                MessageEventType.MESSAGE_EDITED,
                message
        );
    }

    @Test
    void shouldSoftDeleteOwnMessage() {
        Message message = new Message(
                40L,
                300L,
                "Message to delete"
        );

        when(repository.findByIdAndChatId(2L, 40L))
                .thenReturn(Optional.of(message));
        when(repository.saveAndFlush(message))
                .thenReturn(message);

        service.delete(
                40L,
                2L,
                300L,
                TOKEN
        );

        assertThat(message.getContent()).isEmpty();
        assertThat(message.getStatus())
                .isEqualTo(MessageStatus.DELETED);
        assertThat(message.isDeleted()).isTrue();

        verify(repository).saveAndFlush(message);

        verify(eventPublisher).publish(
                MessageEventType.MESSAGE_DELETED,
                message
        );
    }

    @Test
    void shouldRejectEditingAnotherUsersMessage() {
        Message message = new Message(
                50L,
                400L,
                "Another user's message"
        );

        when(repository.findByIdAndChatId(3L, 50L))
                .thenReturn(Optional.of(message));

        assertThatThrownBy(() ->
                service.edit(
                        50L,
                        3L,
                        401L,
                        TOKEN,
                        new EditMessageRequest("Forbidden edit")
                )
        ).isInstanceOf(MessageAccessDeniedException.class)
                .hasMessage(
                        "You cannot modify another user's message"
                );

        verify(repository, never())
                .saveAndFlush(any(Message.class));
    }

    @Test
    void shouldThrowNotFoundWhenMessageDoesNotExist() {
        when(repository.findByIdAndChatId(999L, 60L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.delete(
                        60L,
                        999L,
                        500L,
                        TOKEN
                )
        ).isInstanceOf(MessageNotFoundException.class)
                .hasMessage(
                        "Message with id 999 was not found"
                );

        verify(repository, never())
                .saveAndFlush(any(Message.class));
    }

    @Test
    void shouldRejectEditingDeletedMessage() {
        Message message = new Message(
                70L,
                600L,
                "Deleted message"
        );
        message.softDelete();

        when(repository.findByIdAndChatId(4L, 70L))
                .thenReturn(Optional.of(message));

        assertThatThrownBy(() ->
                service.edit(
                        70L,
                        4L,
                        600L,
                        TOKEN,
                        new EditMessageRequest("New content")
                )
        ).isInstanceOf(IllegalStateException.class)
                .hasMessage("Deleted message cannot be edited");

        verify(repository, never())
                .saveAndFlush(any(Message.class));
    }

    @Test
    void shouldNotSaveMessageWhenMembershipCheckFails() {
        doThrow(
                new AccessDeniedException("Not a chat member")
        ).when(chatMembershipClient)
                .verifyMembership(80L, TOKEN);

        assertThatThrownBy(() ->
                service.send(
                        80L,
                        700L,
                        TOKEN,
                        new SendMessageRequest("Forbidden message")
                )
        ).isInstanceOf(AccessDeniedException.class);

        verify(repository, never())
                .saveAndFlush(any(Message.class));

        verify(messagingTemplate, never())
                .convertAndSend(
                        anyString(),
                        any(Object.class)
                );

        verifyNoInteractions(eventPublisher);
    }
}