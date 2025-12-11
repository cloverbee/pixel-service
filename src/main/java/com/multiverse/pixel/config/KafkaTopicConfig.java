package com.multiverse.pixel.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String PIXEL_EVENTS_TOPIC = "pixel-events";

    @Bean
    public NewTopic pixelEventsTopic() {
        return TopicBuilder.name(PIXEL_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
