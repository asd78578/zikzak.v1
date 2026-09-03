package com.example.zikzak.chatservice.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MessageEventConsumer {

    private final ChatMessageEventHandler eventHandler;

    public MessageEventConsumer(
            ChatMessageEventHandler eventHandler
    ) {
        this.eventHandler = eventHandler;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.message-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(MessageEvent event) {
        eventHandler.handle(event);
    }
}
