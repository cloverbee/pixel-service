package com.multiverse.pixel.controller;

import com.multiverse.pixel.entity.GameState;
import com.multiverse.pixel.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        GameState state = gameService.getGameState();

        return ResponseEntity.ok(Map.of(
                "state", state.getState().toString(),
                "startTime", state.getStartTime() != null ? state.getStartTime() : 0,
                "endTime", state.getEndTime() != null ? state.getEndTime() : 0,
                "remainingSeconds", state.getRemainingSeconds()
        ));
    }

    @PostMapping("/start")
    public ResponseEntity<String> startGame(@RequestBody Map<String, Integer> request) {
        int duration = request.getOrDefault("durationMinutes", 5);
        gameService.startGame(duration);
        return ResponseEntity.ok("Game started for " + duration + " minutes");
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stopGame() {
        gameService.stopGame();
        return ResponseEntity.ok("Game stopped");
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetGame() {
        gameService.resetGame();
        return ResponseEntity.ok("Game reset to WAITING");
    }
}
