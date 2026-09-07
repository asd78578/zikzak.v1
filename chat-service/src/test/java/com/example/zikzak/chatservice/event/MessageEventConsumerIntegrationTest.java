package com.example.zikzak.chatservice.event;

import com.example.zikzak.chatservice.PostgresContainerTest;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
        "spring.kafka.listener.auto-startup=true"
})
@EmbeddedKafka(
        kraft = true,
        partitions = 3,
        topics = {
                "message.events.v1",
                "message.events.v1.DLT"
        },
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class MessageEventConsumerIntegrationTest
        extends PostgresContainerTest {

    private static final String TOPIC =
            "message.events.v1";

    private static final String DLT_TOPIC =
            "message.events.v1.DLT";

    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @MockBean
    private ChatMessageEventHandler eventHandler;

    @Test
    void shouldConsumeAndDeserializeMessageEvent() throws Exception {

        MessageEvent event = new MessageEvent(
                UUID.fromString(
                        "f14a3cce-4df5-4322-a718-f2297de9513e"
                ),
                MessageEventType.MESSAGE_SENT,
                501L,
                77L,
                900L,
                "Kafka message",
                "SENT",
                java.time.OffsetDateTime.parse(
                        "2026-09-03T20:00:00Z"
                )
        );

        kafkaTemplate.send(
                TOPIC,
                "77",
                event
        ).get(10, TimeUnit.SECONDS);

        ArgumentCaptor<MessageEvent> eventCaptor =
                ArgumentCaptor.forClass(MessageEvent.class);

        verify(eventHandler, timeout(10_000))
                .handle(eventCaptor.capture());

        MessageEvent consumedEvent =
                eventCaptor.getValue();

        assertThat(consumedEvent.eventId())
                .isEqualTo(event.eventId());

        assertThat(consumedEvent.type())
                .isEqualTo(MessageEventType.MESSAGE_SENT);

        assertThat(consumedEvent.messageId())
                .isEqualTo(501L);

        assertThat(consumedEvent.chatId())
                .isEqualTo(77L);

        assertThat(consumedEvent.senderAccountId())
                .isEqualTo(900L);

        assertThat(consumedEvent.content())
                .isEqualTo("Kafka message");

        assertThat(consumedEvent.status())
                .isEqualTo("SENT");
    }

    @Test
    void shouldRetryAndSendMessageToDlt()
            throws Exception {

        doThrow(
                new IllegalStateException(
                        "Simulated handler failure"
                )
        )
                .when(eventHandler)
                .handle(any(MessageEvent.class));

        MessageEvent event = new MessageEvent(
                UUID.randomUUID(),
                MessageEventType.MESSAGE_SENT,
                502L,
                88L,
                901L,
                "Message that will fail",
                "SENT",
                java.time.OffsetDateTime.now()
        );

        Map<String, Object> consumerProps =
                KafkaTestUtils.consumerProps(
                        "dlt-test-consumer",
                        "true",
                        embeddedKafkaBroker
                );

        JsonDeserializer<MessageEvent> deserializer =
                new JsonDeserializer<>(
                        MessageEvent.class,
                        false
                );

        deserializer.addTrustedPackages(
                "com.example.zikzak.chatservice.event"
        );

        Consumer<String, MessageEvent> consumer =
                new DefaultKafkaConsumerFactory<>(
                        consumerProps,
                        new StringDeserializer(),
                        deserializer
                )
                        .createConsumer();

        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(
                consumer,
                DLT_TOPIC
        );

        kafkaTemplate.send(
                TOPIC,
                "88",
                event
        ).get(10, TimeUnit.SECONDS);

        verify(
                eventHandler,
                timeout(10_000).times(3)
        )
                .handle(any(MessageEvent.class));

        ConsumerRecord<String, MessageEvent> dltRecord =
                KafkaTestUtils.getSingleRecord(
                        consumer,
                        DLT_TOPIC,
                        java.time.Duration.ofSeconds(10)
                );

        assertThat(dltRecord)
                .isNotNull();

        assertThat(dltRecord.value().messageId())
                .isEqualTo(502L);

        assertThat(dltRecord.value().chatId())
                .isEqualTo(88L);

        assertThat(dltRecord.value().content())
                .isEqualTo(
                        "Message that will fail"
                );

        consumer.close();
    }
}