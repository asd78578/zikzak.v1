package com.example.zikzak.chatservice.event;

import com.example.zikzak.chatservice.chat.Chat;
import com.example.zikzak.chatservice.chat.ChatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatMessageEventHandlerTest {

    private static final OffsetDateTime EVENT_TIME =
            OffsetDateTime.parse("2026-09-03T20:00:00Z");

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private ProcessedMessageEventRepository processedMessageEventRepository;

    private Chat chat;
    private ChatMessageEventHandler handler;

    @BeforeEach
    void setUp() {
        chat = new Chat("100:200");

        handler = new ChatMessageEventHandler(
                chatRepository,
                processedMessageEventRepository
        );
    }

    @Test
    void shouldApplySentMessage() {
        MessageEvent event = event(
                MessageEventType.MESSAGE_SENT,
                501L,
                "Hello Kafka",
                "SENT"
        );

        when(chatRepository.findById(10L))
                .thenReturn(Optional.of(chat));

        handler.handle(event);

        assertThat(chat.getLastMessageId()).isEqualTo(501L);
        assertThat(chat.getLastMessagePreview())
                .isEqualTo("Hello Kafka");
        assertThat(chat.getLastMessageAt())
                .isEqualTo(EVENT_TIME);

        verify(chatRepository).findById(10L);
        verify(processedMessageEventRepository)
                .save(any(ProcessedMessageEvent.class));
    }

    @Test
    void shouldApplyEditedMessageWhenItIsLast() {
        chat.applySentMessage(
                501L,
                "Old content",
                EVENT_TIME
        );

        when(chatRepository.findById(10L))
                .thenReturn(Optional.of(chat));

        handler.handle(event(
                MessageEventType.MESSAGE_EDITED,
                501L,
                "New content",
                "EDITED"
        ));

        assertThat(chat.getLastMessageId()).isEqualTo(501L);
        assertThat(chat.getLastMessagePreview())
                .isEqualTo("New content");
        assertThat(chat.getLastMessageAt())
                .isEqualTo(EVENT_TIME);
    }

    @Test
    void shouldApplyDeletedMessageWhenItIsLast() {
        chat.applySentMessage(
                501L,
                "Message to delete",
                EVENT_TIME
        );

        when(chatRepository.findById(10L))
                .thenReturn(Optional.of(chat));

        handler.handle(event(
                MessageEventType.MESSAGE_DELETED,
                501L,
                "",
                "DELETED"
        ));

        assertThat(chat.getLastMessageId()).isEqualTo(501L);
        assertThat(chat.getLastMessagePreview())
                .isEqualTo("Сообщение удалено");
        assertThat(chat.getLastMessageAt())
                .isEqualTo(EVENT_TIME);
    }

    @Test
    void shouldIgnoreEditWhenMessageIsNotLast() {
        chat.applySentMessage(
                502L,
                "Current last message",
                EVENT_TIME
        );

        when(chatRepository.findById(10L))
                .thenReturn(Optional.of(chat));

        handler.handle(event(
                MessageEventType.MESSAGE_EDITED,
                501L,
                "Edited old message",
                "EDITED"
        ));

        assertThat(chat.getLastMessageId()).isEqualTo(502L);
        assertThat(chat.getLastMessagePreview())
                .isEqualTo("Current last message");
    }

    @Test
    void shouldThrowWhenChatDoesNotExist() {
        when(chatRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                handler.handle(event(
                        MessageEventType.MESSAGE_SENT,
                        501L,
                        "Message",
                        "SENT"
                ))
        ).isInstanceOf(IllegalStateException.class)
                .hasMessage("Chat with id 10 was not found");

        verify(processedMessageEventRepository, never())
                .save(any());
    }

    @Test
    void shouldSaveProcessedEventAfterSuccessfulHandling() {
        UUID eventId = UUID.randomUUID();

        MessageEvent event = new MessageEvent(
                eventId,
                MessageEventType.MESSAGE_SENT,
                501L,
                10L,
                100L,
                "Hello Kafka",
                "SENT",
                EVENT_TIME
        );

        when(chatRepository.findById(10L))
                .thenReturn(Optional.of(chat));

        handler.handle(event);

        ArgumentCaptor<ProcessedMessageEvent> captor =
                ArgumentCaptor.forClass(ProcessedMessageEvent.class);

        verify(processedMessageEventRepository)
                .save(captor.capture());

        ProcessedMessageEvent savedEvent = captor.getValue();

        assertThat(savedEvent.getEventId())
                .isEqualTo(eventId);

        assertThat(savedEvent.getProcessedAt())
                .isNotNull();
    }

    @Test
    void shouldSkipAlreadyProcessedEvent() {
        UUID eventId = UUID.randomUUID();

        MessageEvent event = new MessageEvent(
                eventId,
                MessageEventType.MESSAGE_SENT,
                501L,
                10L,
                100L,
                "Duplicate message",
                "SENT",
                EVENT_TIME
        );

        when(processedMessageEventRepository.existsById(eventId))
                .thenReturn(true);

        handler.handle(event);

        verify(chatRepository, never())
                .findById(any());

        verify(processedMessageEventRepository, never())
                .save(any());

        assertThat(chat.getLastMessageId()).isNull();
        assertThat(chat.getLastMessagePreview()).isNull();
    }

    private MessageEvent event(
            MessageEventType type,
            Long messageId,
            String content,
            String status
    ) {
        return new MessageEvent(
                UUID.randomUUID(),
                type,
                messageId,
                10L,
                100L,
                content,
                status,
                EVENT_TIME
        );
    }
}