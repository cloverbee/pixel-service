package com.multiverse.pixel.consumer;

import com.multiverse.pixel.config.KafkaTopicConfig;
import com.multiverse.pixel.dto.PixelEvent;
import com.multiverse.pixel.repository.BoardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class StateConsumer {

    private static final Logger log = LoggerFactory.getLogger(StateConsumer.class);
    private final BoardRepository boardRepository;

    public StateConsumer(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @KafkaListener(
            topics = KafkaTopicConfig.PIXEL_EVENTS_TOPIC,
            groupId = "state-updater",
            concurrency = "3" // Match number of partitions for max throughput
    )
    public void consumePixelEvent(PixelEvent event) {
        log.info("📥 Consuming pixel event: x={}, y={}, color={}, userId={}",
                event.getX(), event.getY(), event.getColor(), event.getUserId());

        // Update Redis with new pixel color
        boardRepository.updatePixel(event.getX(), event.getY(), event.getColor());
        
        log.info("💾 Updated Redis: {}:{} -> {}", event.getX(), event.getY(), event.getColor());
    }
}