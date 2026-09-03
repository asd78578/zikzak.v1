package com.example.zikzak.chatservice.event;

import com.example.zikzak.chatservice.PostgresContainerTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;


@SpringBootTest(properties = {
        "spring.kafka.listener.auto-startup=true",
        "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
        "spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer"
})
@EmbeddedKafka(
        kraft = true,
        partitions = 3,
        topics = "message.events.v1",
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class MessageEventConsumerIntegrationTest
        extends PostgresContainerTest {

    private static final String TOPIC = "message.events.v1";

    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @MockBean
    private ChatMessageEventHandler eventHandler;

    @Test
    void shouldConsumeAndDeserializeMessageEvent() throws Exception {
        String json = """
                {
                  "eventId": "f14a3cce-4df5-4322-a718-f2297de9513e",
                  "type": "MESSAGE_SENT",
                  "messageId": 501,
                  "chatId": 77,
                  "senderAccountId": 900,
                  "content": "Kafka message",
                  "status": "SENT",
                  "occurredAt": "2026-09-03T20:00:00Z"
                }
                """;

        kafkaTemplate.send(
                TOPIC,
                "77",
                json
        ).get(10, TimeUnit.SECONDS);

        ArgumentCaptor<MessageEvent> eventCaptor =
                ArgumentCaptor.forClass(MessageEvent.class);

        verify(eventHandler, timeout(10_000))
                .handle(eventCaptor.capture());

        MessageEvent event = eventCaptor.getValue();

        assertThat(event.eventId().toString())
                .isEqualTo(
                        "f14a3cce-4df5-4322-a718-f2297de9513e"
                );
        assertThat(event.type())
                .isEqualTo(MessageEventType.MESSAGE_SENT);
        assertThat(event.messageId()).isEqualTo(501L);
        assertThat(event.chatId()).isEqualTo(77L);
        assertThat(event.senderAccountId()).isEqualTo(900L);
        assertThat(event.content()).isEqualTo("Kafka message");
        assertThat(event.status()).isEqualTo("SENT");
        assertThat(event.occurredAt().toString())
                .isEqualTo("2026-09-03T20:00Z");
    }
}
