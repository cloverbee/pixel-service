package com.multiverse.pixel.repository;

import com.multiverse.pixel.consumer.StateConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Repository
public class BoardRepository {

    private static final String BOARD_KEY = "BOARD_STATE";
    private final StringRedisTemplate redisTemplate;

    private static final Logger log = LoggerFactory.getLogger(BoardRepository.class);


    public BoardRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Update a single pixel on the board.
     * Uses Redis Hash structure: HSET BOARD_STATE "x:y" "#FF5733"
     */
    public void updatePixel(int x, int y, String color) {
        String field = x + ":" + y;
        log.info("updatePixel----- field = {}",
                field);
        redisTemplate.opsForHash().put(BOARD_KEY, field, color);
    }

    /**
     * Retrieve the entire board state.
     * Returns a map where key is "x:y" and value is color hex code.
     */
    public Map<Object, Object> getBoard() {
        return redisTemplate.opsForHash().entries(BOARD_KEY);
    }

    /**
     * Clear the entire board (useful for testing).
     */
    public void clearBoard() {
        redisTemplate.delete(BOARD_KEY);
    }
}