package com.example.zikzak.chatservice.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class MessageEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(MessageEventConsumer.class);

    private static final String CORRELATION_ID_HEADER =
            "X-Correlation-Id";

    private static final String CORRELATION_ID_MDC_KEY =
            "correlationId";

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
    public void consume(
            MessageEvent event,
            @Header(
                    name = CORRELATION_ID_HEADER,
                    required = false
            ) byte[] correlationIdHeader
    ) {
        String correlationId = resolveCorrelationId(
                correlationIdHeader,
                event
        );

        MDC.put(
                CORRELATION_ID_MDC_KEY,
                correlationId
        );

        try {
            log.info(
                    "Kafka event received: eventId={}, messageId={}, chatId={}, type={}",
                    event.eventId(),
                    event.messageId(),
                    event.chatId(),
                    event.type()
            );

            MessageEventHandlingResult result =
                    eventHandler.handle(event);

            if (result == MessageEventHandlingResult.PROCESSED) {
                kafkaMetrics.incrementProcessed();

                log.info(
                        "Kafka event processed: eventId={}, chatId={}",
                        event.eventId(),
                        event.chatId()
                );
            }

        } catch (RuntimeException ex) {
            kafkaMetrics.incrementFailed();

            log.error(
                    "Kafka event processing failed: eventId={}, chatId={}",
                    event.eventId(),
                    event.chatId(),
                    ex
            );

            throw ex;

        } finally {
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }

    private String resolveCorrelationId(
            byte[] correlationIdHeader,
            MessageEvent event
    ) {
        if (correlationIdHeader != null
                && correlationIdHeader.length > 0) {

            return new String(
                    correlationIdHeader,
                    StandardCharsets.UTF_8
            );
        }

        return event.eventId().toString();
    }
}