package com.multiverse.pixel.service;

import com.multiverse.pixel.config.KafkaTopicConfig;
import com.multiverse.pixel.dto.PixelEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PixelEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PixelEventProducer.class);
    private final KafkaTemplate<String, PixelEvent> kafkaTemplate;

    public PixelEventProducer(KafkaTemplate<String, PixelEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Send a pixel event to Kafka.
     * Key is "x:y" for partitioning (pixels in same location go to same partition).
     */
    public void sendPixelEvent(PixelEvent event) {
        String key = event.getX() + ":" + event.getY();

        kafkaTemplate.send(KafkaTopicConfig.PIXEL_EVENTS_TOPIC, key, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("✅ Sent pixel event to Kafka: x={}, y={}, color={}, partition={}",
                                event.getX(), event.getY(), event.getColor(), result.getRecordMetadata().partition());
                    } else {
                        log.error("❌ Failed to send pixel event", ex);
                    }
                });
    }
}