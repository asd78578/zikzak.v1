package com.example.zikzak.chatservice.event;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class KafkaMetrics {

    private final Counter processedCounter;
    private final Counter duplicateCounter;
    private final Counter failedCounter;

    private final Counter dltCounter;

    public KafkaMetrics(MeterRegistry meterRegistry) {
        this.processedCounter = Counter.builder("zikzak.kafka.events.processed")
                .description("Number of successfully processed Kafka message events")
                .register(meterRegistry);

        this.duplicateCounter = Counter.builder("zikzak.kafka.events.duplicate")
                .description("Number of duplicate Kafka message events")
                .register(meterRegistry);

        this.failedCounter = Counter.builder("zikzak.kafka.events.failed")
                .description("Number of failed Kafka message events")
                .register(meterRegistry);

        this.dltCounter = Counter.builder("zikzak.kafka.events.dlt")
                .description("Number of Kafka message events received from DLT")
                .register(meterRegistry);
    }

    public void incrementProcessed() {
        processedCounter.increment();
    }

    public void incrementDuplicate() {
        duplicateCounter.increment();
    }

    public void incrementFailed() {
        failedCounter.increment();
    }

    public void incrementDlt() {
        dltCounter.increment();
    }
}