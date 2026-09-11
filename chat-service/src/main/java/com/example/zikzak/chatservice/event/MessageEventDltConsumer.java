package com.example.zikzak.chatservice.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class MessageEventDltConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(MessageEventDltConsumer.class);

    private static final String CORRELATION_ID_HEADER =
            "X-Correlation-Id";

    private static final String CORRELATION_ID_MDC_KEY =
            "correlationId";

    private final KafkaMetrics kafkaMetrics;

    public MessageEventDltConsumer(
            KafkaMetrics kafkaMetrics
    ) {
        this.kafkaMetrics = kafkaMetrics;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.message-events-dlt}",
            groupId = "${spring.kafka.consumer.group-id}-dlt"
    )
    public void consume(
            MessageEvent event,

            @Header(KafkaHeaders.RECEIVED_TOPIC)
            String topic,

            @Header(KafkaHeaders.RECEIVED_PARTITION)
            int partition,

            @Header(KafkaHeaders.OFFSET)
            long offset,

            @Header(
                    name = KafkaHeaders.DLT_EXCEPTION_FQCN,
                    required = false
            )
            String exceptionClass,

            @Header(
                    name = KafkaHeaders.DLT_EXCEPTION_MESSAGE,
                    required = false
            )
            String exceptionMessage,

            @Header(
                    name = CORRELATION_ID_HEADER,
                    required = false
            )
            byte[] correlationIdHeader
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
            kafkaMetrics.incrementDlt();

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