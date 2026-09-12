package com.example.zikzak.messageservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class KafkaObservationConfig {

    @Bean
    public Object kafkaTemplateObservationConfigurer(
            KafkaTemplate<?, ?> kafkaTemplate
    ) {
        kafkaTemplate.setObservationEnabled(true);
        return new Object();
    }
}
