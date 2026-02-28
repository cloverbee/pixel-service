package com.multiverse.pixel.consumer;

import com.multiverse.pixel.config.KafkaTopicConfig;
import com.multiverse.pixel.dto.PixelEvent;
import com.multiverse.pixel.service.ScoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class StatsConsumer {

    private static final Logger log = LoggerFactory.getLogger(StatsConsumer.class);
    private final ScoreService scoreService;

    public StatsConsumer(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    @KafkaListener(
            topics = KafkaTopicConfig.PIXEL_EVENTS_TOPIC,
            groupId = "stats-counter", // Third independent consumer group
            concurrency = "2"
    )
    public void updateStats(PixelEvent event) {
        log.debug("Updating stats for user: {}", event.getUserId());

        // Atomically increment user's score
        scoreService.incrementScore(event.getUserId());
    }
}