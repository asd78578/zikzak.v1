package com.example.zikzak.chatservice.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;


@Component
public class MessageEventDltConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(MessageEventDltConsumer.class);

    @KafkaListener(
            topics = "${app.kafka.topics.message-events-dlt}",
            groupId = "${spring.kafka.consumer.group-id}-dlt"
    )
    public void consume(
            MessageEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(
                    name = KafkaHeaders.DLT_EXCEPTION_FQCN,
                    required = false
            ) String exceptionClass,
            @Header(
                    name = KafkaHeaders.DLT_EXCEPTION_MESSAGE,
                    required = false
            ) String exceptionMessage
    ) {

        log.error(
                "Message received from DLT: eventId={}, messageId={}, chatId={}, topic={}, partition={}, offset={}, exceptionClass={}, exceptionMessage={}",
                event.eventId(),
                event.messageId(),
                event.chatId(),
                topic,
                partition,
                offset,
                exceptionClass,
                exceptionMessage
        );
    }
}