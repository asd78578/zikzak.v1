package com.example.zikzak.chatservice.event;

import com.example.zikzak.chatservice.PostgresContainerTest;
import com.example.zikzak.chatservice.chat.Chat;
import com.example.zikzak.chatservice.chat.ChatMemberRepository;
import com.example.zikzak.chatservice.chat.ChatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ChatMessageEventHandlerIntegrationTest
        extends PostgresContainerTest {

    private static final OffsetDateTime EVENT_TIME =
            OffsetDateTime.parse("2026-09-08T20:00:00Z");

    @Autowired
    private ChatMessageEventHandler handler;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ChatMemberRepository memberRepository;

    @Autowired
    private ProcessedMessageEventRepository processedEventRepository;

    @BeforeEach
    void cleanDatabase() {
        processedEventRepository.deleteAll();
        memberRepository.deleteAll();
        chatRepository.deleteAll();
    }

    @Test
    void shouldProcessEventOnlyOnce() {
        Chat chat = new Chat("100:200");
        chat.addMember(100L);
        chat.addMember(200L);

        Chat savedChat = chatRepository.saveAndFlush(chat);

        UUID eventId = UUID.randomUUID();

        MessageEvent firstDelivery = new MessageEvent(
                eventId,
                MessageEventType.MESSAGE_SENT,
                501L,
                savedChat.getId(),
                100L,
                "Original message",
                "SENT",
                EVENT_TIME
        );

        handler.handle(firstDelivery);

        Chat afterFirstDelivery = chatRepository
                .findById(savedChat.getId())
                .orElseThrow();

        assertThat(afterFirstDelivery.getLastMessageId())
                .isEqualTo(501L);

        assertThat(afterFirstDelivery.getLastMessagePreview())
                .isEqualTo("Original message");

        assertThat(processedEventRepository.existsById(eventId))
                .isTrue();

        assertThat(processedEventRepository.count())
                .isEqualTo(1);

        MessageEvent duplicateDelivery = new MessageEvent(
                eventId,
                MessageEventType.MESSAGE_SENT,
                501L,
                savedChat.getId(),
                100L,
                "THIS MUST NOT BE APPLIED",
                "SENT",
                EVENT_TIME.plusMinutes(1)
        );

        handler.handle(duplicateDelivery);

        Chat afterDuplicate = chatRepository
                .findById(savedChat.getId())
                .orElseThrow();

        assertThat(afterDuplicate.getLastMessagePreview())
                .isEqualTo("Original message");

        assertThat(afterDuplicate.getLastMessageAt())
                .isEqualTo(EVENT_TIME);

        assertThat(processedEventRepository.count())
                .isEqualTo(1);
    }

    @Test
    void shouldNotMarkEventAsProcessedWhenHandlingFails() {
        UUID eventId = UUID.randomUUID();

        MessageEvent event = new MessageEvent(
                eventId,
                MessageEventType.MESSAGE_SENT,
                501L,
                999999L,
                100L,
                "Message",
                "SENT",
                EVENT_TIME
        );

        assertThatThrownBy(() -> handler.handle(event))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Chat with id 999999 was not found");

        assertThat(processedEventRepository.existsById(eventId))
                .isFalse();

        assertThat(processedEventRepository.count())
                .isZero();
    }
}
