package com.example.zikzak.messageservice.event;

import com.example.zikzak.messageservice.message.Message;
import com.example.zikzak.messageservice.message.MessageStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MessageEventPublisherTest {

    @Mock
    private KafkaTemplate<String, MessageEvent> kafkaTemplate;

    @Test
    void shouldPublishMessageEventUsingChatIdAsKey() {
        Message message = new Message(
                10L,
                100L,
                "Hello Kafka"
        );

        ReflectionTestUtils.setField(
                message,
                "id",
                99L
        );

        MessageEventPublisher publisher =
                new MessageEventPublisher(
                        kafkaTemplate,
                        "message.events.v1"
                );

        publisher.publish(
                MessageEventType.MESSAGE_SENT,
                message
        );

        ArgumentCaptor<MessageEvent> eventCaptor =
                ArgumentCaptor.forClass(MessageEvent.class);

        verify(kafkaTemplate).send(
                eq("message.events.v1"),
                eq("10"),
                eventCaptor.capture()
        );

        MessageEvent event = eventCaptor.getValue();

        assertThat(event.eventId()).isNotNull();
        assertThat(event.type())
                .isEqualTo(MessageEventType.MESSAGE_SENT);
        assertThat(event.messageId()).isEqualTo(99L);
        assertThat(event.chatId()).isEqualTo(10L);
        assertThat(event.senderAccountId()).isEqualTo(100L);
        assertThat(event.content()).isEqualTo("Hello Kafka");
        assertThat(event.status()).isEqualTo(MessageStatus.SENT);
        assertThat(event.occurredAt()).isNotNull();
    }
}
