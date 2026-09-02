package com.example.zikzak.messageservice.event;

import com.example.zikzak.messageservice.message.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class MessageEventPublisher {

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

        kafkaTemplate.send(
                topicName,
                message.getChatId().toString(),
                event
        );
    }
}
