package com.multiverse.pixel.controller;

import com.multiverse.pixel.dto.PixelEvent;
import com.multiverse.pixel.entity.GameState;
import com.multiverse.pixel.service.GameService;
import com.multiverse.pixel.service.PixelEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.multiverse.pixel.repository.BoardRepository;
import java.util.Map;

import com.multiverse.pixel.service.RateLimiterService;



@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allow frontend to call from different port
public class PixelController {

    private static final Logger log = LoggerFactory.getLogger(PixelController.class);
    private final PixelEventProducer producer;
    private final BoardRepository boardRepository;
    private final GameService gameService;// Add field
    private final RateLimiterService rateLimiterService;


    // Update constructor
    public PixelController(PixelEventProducer producer, BoardRepository boardRepository, GameService gameService, RateLimiterService rateLimiterService) {
        this.producer = producer;
        this.boardRepository = boardRepository;
        this.gameService = gameService;
        this.rateLimiterService = rateLimiterService;
    }

    // Add new endpoint
    @GetMapping("/board")
    public ResponseEntity<Map<Object, Object>> getBoard() {
        Map<Object, Object> board = boardRepository.getBoard();
        return ResponseEntity.ok(board);
    }

    @PostMapping("/pixels")
    public ResponseEntity<String> paintPixel(@RequestBody PixelEvent event) {
        log.info("🎨 Received POST /api/pixels: x={}, y={}, color={}, userId={}",
                event.getX(), event.getY(), event.getColor(), event.getUserId());

        // Check game state first
        GameState gameState = gameService.getGameState();
        if (!gameState.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Game is not active. Current state: " + gameState.getState());
        }

        // Check rate limit
        if (!rateLimiterService.isAllowed(event.getUserId())) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-RateLimit-Limit", "100");
            headers.add("X-RateLimit-Remaining", "0");
            headers.add("Retry-After", "1");

            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .headers(headers)
                    .body("Rate limit exceeded. Max 100 requests per second.");
        }

        // Validate input
        if (event.getX() < 0 || event.getX() >= 50) {
            return ResponseEntity.badRequest().body("X must be between 0 and 49");
        }
        if (event.getY() < 0 || event.getY() >= 50) {
            return ResponseEntity.badRequest().body("Y must be between 0 and 49");
        }
        if (event.getColor() == null || !event.getColor().matches("^#[0-9A-Fa-f]{6}$")) {
            return ResponseEntity.badRequest().body("Color must be hex format (e.g., #FF5733)");
        }
        if (event.getUserId() == null || event.getUserId().isEmpty()) {
            return ResponseEntity.badRequest().body("UserId is required");
        }

        // Send to Kafka
        producer.sendPixelEvent(event);

        // Add rate limit headers to success response
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-RateLimit-Limit", "100");
        headers.add("X-RateLimit-Remaining",
                String.valueOf(rateLimiterService.getRemaining(event.getUserId())));
        // Return 202 Accepted (request received, will be processed asynchronously)
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Pixel event accepted in pixel controller");
    }
}


