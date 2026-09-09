package com.example.zikzak.chatservice.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MessageEventConsumer {

    private final ChatMessageEventHandler eventHandler;
    private final KafkaMetrics kafkaMetrics;

    public MessageEventConsumer(
            ChatMessageEventHandler eventHandler,
            KafkaMetrics kafkaMetrics
    ) {
        this.eventHandler = eventHandler;
        this.kafkaMetrics = kafkaMetrics;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.message-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(MessageEvent event) {
        try {
            MessageEventHandlingResult result =
                    eventHandler.handle(event);

            if (result == MessageEventHandlingResult.PROCESSED) {
                kafkaMetrics.incrementProcessed();
            }

        } catch (RuntimeException ex) {
            kafkaMetrics.incrementFailed();
            throw ex;
        }
    }
}