package com.example.zikzak.messageservice.event;

import com.example.zikzak.messageservice.message.Message;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class MessageEventPublisher {

    private static final String CORRELATION_ID_HEADER =
            "X-Correlation-Id";

    private static final String CORRELATION_ID_MDC_KEY =
            "correlationId";

    private final KafkaTemplate<String, MessageEvent> kafkaTemplate;
    private final String topicName;

    public MessageEventPublisher(
            KafkaTemplate<String, MessageEvent> kafkaTemplate,
            @Value("${app.kafka.topics.message-events}")
            String topicName
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    public void publish(
            MessageEventType type,
            Message message
    ) {
        MessageEvent event = new MessageEvent(
                UUID.randomUUID(),
                type,
                message.getId(),
                message.getChatId(),
                message.getSenderAccountId(),
                message.getContent(),
                message.getStatus(),
                OffsetDateTime.now()
        );

        String correlationId =
                MDC.get(CORRELATION_ID_MDC_KEY);

        RecordHeaders headers = new RecordHeaders();

        if (correlationId != null && !correlationId.isBlank()) {
            headers.add(
                    CORRELATION_ID_HEADER,
                    correlationId.getBytes(StandardCharsets.UTF_8)
            );
        }

        ProducerRecord<String, MessageEvent> record =
                new ProducerRecord<>(
                        topicName,
                        null,
                        message.getChatId().toString(),
                        event,
                        headers
                );

        kafkaTemplate.send(record);
    }
}
