package com.multiverse.pixel.controller;

import com.multiverse.pixel.entity.GameState;
import com.multiverse.pixel.entity.Team;
import com.multiverse.pixel.service.GameService;
import com.multiverse.pixel.service.TeamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/team")
@CrossOrigin(origins = "*")
public class TeamController {

    private final TeamService teamService;
    private final GameService gameService;

    public TeamController(TeamService teamService, GameService gameService) {
        this.teamService = teamService;
        this.gameService = gameService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerTeam(@RequestBody Team team) {
        // Only allow registration in WAITING state
        GameState state = gameService.getGameState();
        if (state.getState() != GameState.State.WAITING) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Can only register teams before game starts");
        }

        // Validate team data
        if (team.getTeamId() == null || !team.getTeamId().matches("^[a-zA-Z0-9-]+$")) {
            return ResponseEntity.badRequest()
                    .body("TeamId must be alphanumeric with hyphens only");
        }

        if (team.getColor() == null || !team.getColor().matches("^#[0-9A-Fa-f]{6}$")) {
            return ResponseEntity.badRequest()
                    .body("Color must be hex format (e.g., #FF5733)");
        }

        // Check if team already exists
        if (teamService.getTeam(team.getTeamId()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Team ID already registered");
        }

        teamService.registerTeam(team);
        return ResponseEntity.ok("Team registered successfully");
    }

    @GetMapping("/list")
    public ResponseEntity<List<Team>> listTeams() {
        return ResponseEntity.ok(teamService.getAllTeams());
    }
}