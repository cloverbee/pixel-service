package com.multiverse.pixel.consumer;

import com.multiverse.pixel.config.KafkaTopicConfig;
import com.multiverse.pixel.dto.PixelEvent;
import com.multiverse.pixel.entity.PixelHistoryEntity;
import com.multiverse.pixel.repository.PixelHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AuditConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditConsumer.class);
    private final PixelHistoryRepository historyRepository;

    public AuditConsumer(PixelHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @KafkaListener(
            topics = KafkaTopicConfig.PIXEL_EVENTS_TOPIC,
            groupId = "audit-logger", // Different group = independent consumption
            concurrency = "2"
    )
    public void auditPixelEvent(PixelEvent event) {
        log.debug("Auditing pixel event: x={}, y={}, userId={}",
                event.getX(), event.getY(), event.getUserId());

        // Convert to entity
        PixelHistoryEntity entity = new PixelHistoryEntity(
                event.getX(),
                event.getY(),
                Instant.now(),
                event.getUserId(),
                event.getColor()
        );

        // Save to Cassandra (async, won't block Redis updates)
        historyRepository.save(entity);
    }
}