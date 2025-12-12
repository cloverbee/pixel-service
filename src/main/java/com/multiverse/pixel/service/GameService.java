package com.multiverse.pixel.service;

import com.multiverse.pixel.entity.GameState;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class GameService {

    private static final String GAME_STATE_KEY = "GAME_STATE";
    private final RedisTemplate<String, Object> redisTemplate;

    public GameService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public GameState getGameState() {
        GameState state = (GameState) redisTemplate.opsForValue().get(GAME_STATE_KEY);
        if (state == null) {
            state = new GameState();
            redisTemplate.opsForValue().set(GAME_STATE_KEY, state);
        }

        // Auto-transition to FINISHED if time expired
//        if (state.isActive() && state.getRemainingSeconds() <= 0) {
//            state.setState(GameState.State.FINISHED);
//            redisTemplate.opsForValue().set(GAME_STATE_KEY, state);
//        }

        if (state.getState() == GameState.State.ACTIVE
                && state.getEndTime() != null
                && System.currentTimeMillis() / 1000 >= state.getEndTime()) {
            state.setState(GameState.State.FINISHED);
            redisTemplate.opsForValue().set(GAME_STATE_KEY, state);
        }

        return state;
    }

    public void startGame(int durationMinutes) {
        GameState state = new GameState();
        state.setState(GameState.State.ACTIVE);

        long now = System.currentTimeMillis() / 1000;
        state.setStartTime(now);
        state.setEndTime(now + (durationMinutes * 60));
        state.setDurationSeconds(durationMinutes * 60);

        redisTemplate.opsForValue().set(GAME_STATE_KEY, state);
    }

    public void stopGame() {
        GameState state = getGameState();
        state.setState(GameState.State.FINISHED);
        redisTemplate.opsForValue().set(GAME_STATE_KEY, state);
    }

    public void resetGame() {
        GameState state = new GameState();
        state.setState(GameState.State.WAITING);
        redisTemplate.opsForValue().set(GAME_STATE_KEY, state);
    }
}