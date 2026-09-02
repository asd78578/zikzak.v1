package com.example.zikzak.messageservice.event;

import com.example.zikzak.messageservice.message.Message;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = {
        KafkaAutoConfiguration.class,
        MessageEventPublisher.class
})
@TestPropertySource(properties = {
        "app.kafka.topics.message-events=message.events.v1",
        "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
        "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer"
})
@EmbeddedKafka(
        kraft = true,
        partitions = 3,
        topics = "message.events.v1",
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class MessageEventKafkaIntegrationTest {

    private static final String TOPIC = "message.events.v1";

    @Autowired
    private MessageEventPublisher eventPublisher;

    @Autowired
    private KafkaTemplate<String, MessageEvent> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Test
    void shouldPublishMessageEventAsJson() throws Exception {
        Map<String, Object> consumerProperties =
                KafkaTestUtils.consumerProps(
                        "message-event-test",
                        "true",
                        embeddedKafka
                );

        consumerProperties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        try (Consumer<String, String> consumer =
                     new DefaultKafkaConsumerFactory<>(
                             consumerProperties,
                             new StringDeserializer(),
                             new StringDeserializer()
                     ).createConsumer()) {

            embeddedKafka.consumeFromAnEmbeddedTopic(
                    consumer,
                    TOPIC
            );

            Message message = new Message(
                    77L,
                    900L,
                    "Kafka message"
            );

            ReflectionTestUtils.setField(
                    message,
                    "id",
                    501L
            );

            eventPublisher.publish(
                    MessageEventType.MESSAGE_SENT,
                    message
            );

            kafkaTemplate.flush();

            ConsumerRecord<String, String> record =
                    KafkaTestUtils.getSingleRecord(
                            consumer,
                            TOPIC,
                            Duration.ofSeconds(10)
                    );

            assertThat(record.key()).isEqualTo("77");

            JsonNode json =
                    new ObjectMapper().readTree(record.value());

            assertThat(json.get("eventId").asText()).isNotBlank();
            assertThat(json.get("type").asText())
                    .isEqualTo("MESSAGE_SENT");
            assertThat(json.get("messageId").asLong())
                    .isEqualTo(501L);
            assertThat(json.get("chatId").asLong())
                    .isEqualTo(77L);
            assertThat(json.get("senderAccountId").asLong())
                    .isEqualTo(900L);
            assertThat(json.get("content").asText())
                    .isEqualTo("Kafka message");
            assertThat(json.get("status").asText())
                    .isEqualTo("SENT");
            assertThat(json.get("occurredAt").asText())
                    .isNotBlank();
        }
    }
}
