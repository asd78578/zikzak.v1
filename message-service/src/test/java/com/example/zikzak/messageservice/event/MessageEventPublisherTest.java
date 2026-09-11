package com.example.zikzak.messageservice.event;

import com.example.zikzak.messageservice.message.Message;
import com.example.zikzak.messageservice.message.MessageStatus;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MessageEventPublisherTest {

    @Mock
    private KafkaTemplate<String, MessageEvent> kafkaTemplate;

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void shouldPublishMessageEventUsingChatIdAsKeyAndCorrelationIdHeader() {
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

        MDC.put(
                "correlationId",
                "day38-test-123"
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

        @SuppressWarnings("unchecked")
        ArgumentCaptor<ProducerRecord<String, MessageEvent>> recordCaptor =
                ArgumentCaptor.forClass(ProducerRecord.class);

        verify(kafkaTemplate).send(
                recordCaptor.capture()
        );

        ProducerRecord<String, MessageEvent> record =
                recordCaptor.getValue();

        assertThat(record.topic())
                .isEqualTo("message.events.v1");

        assertThat(record.key())
                .isEqualTo("10");

        MessageEvent event = record.value();

        assertThat(event.eventId()).isNotNull();
        assertThat(event.type())
                .isEqualTo(MessageEventType.MESSAGE_SENT);
        assertThat(event.messageId()).isEqualTo(99L);
        assertThat(event.chatId()).isEqualTo(10L);
        assertThat(event.senderAccountId()).isEqualTo(100L);
        assertThat(event.content()).isEqualTo("Hello Kafka");
        assertThat(event.status()).isEqualTo(MessageStatus.SENT);
        assertThat(event.occurredAt()).isNotNull();

        Header correlationIdHeader =
                record.headers()
                        .lastHeader("X-Correlation-Id");

        assertThat(correlationIdHeader)
                .isNotNull();

        assertThat(
                new String(
                        correlationIdHeader.value(),
                        StandardCharsets.UTF_8
                )
        ).isEqualTo("day38-test-123");
    }
}