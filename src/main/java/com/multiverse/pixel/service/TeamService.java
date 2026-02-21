package com.multiverse.pixel.service;


import com.multiverse.pixel.entity.Team;
import com.multiverse.pixel.repository.BoardRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeamService {

    private static final String TEAMS_KEY = "TEAMS";
    private final RedisTemplate<String, Object> redisTemplate;
    private final BoardRepository boardRepository;
    //private final ScoreService scoreService;

    public TeamService(RedisTemplate<String, Object> redisTemplate,
                       BoardRepository boardRepository
            //,
                       //ScoreService scoreService
    ) {
        this.redisTemplate = redisTemplate;
        this.boardRepository = boardRepository;
        //this.scoreService = scoreService;
    }

    public void registerTeam(Team team) {
        redisTemplate.opsForHash().put(TEAMS_KEY, team.getTeamId(), team);
    }

    public Team getTeam(String teamId) {
        return (Team) redisTemplate.opsForHash().get(TEAMS_KEY, teamId);
    }

    public List<Team> getAllTeams() {
        return redisTemplate.opsForHash().values(TEAMS_KEY).stream()
                .map(obj -> (Team) obj)
                .collect(Collectors.toList());
    }

    /**
     * Calculate final results based on territory control.
     * Counts how many pixels each team currently owns on the board.
     */
    public Map<String, Object> calculateResults() {
        Map<Object, Object> board = boardRepository.getBoard();
        //List<Map<String, Object>> leaderboard = scoreService.getLeaderboard(100);

        // Count pixels by color
        Map<String, Integer> colorCounts = new HashMap<>();
        for (Object colorObj : board.values()) {
            String color = (String) colorObj;
            colorCounts.put(color, colorCounts.getOrDefault(color, 0) + 1);
        }

        // Map colors to teams and build results
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Team> teamsByColor = new HashMap<>();

        for (Team team : getAllTeams()) {
            teamsByColor.put(team.getColor(), team);
        }

        for (Map.Entry<String, Integer> entry : colorCounts.entrySet()) {
            String color = entry.getKey();
            int pixelCount = entry.getValue();

            Team team = teamsByColor.get(color);
            if (team != null) {
                // Find total paints from leaderboard
//                long totalPaints = leaderboard.stream()
//                        .filter(s -> s.get("userId").equals(team.getTeamId()))
//                        .findFirst()
//                        .map(s -> (Long) s.get("score"))
//                        .orElse(0L);

                results.add(Map.of(
                        "teamId", team.getTeamId(),
                        "teamName", team.getTeamName(),
                        "color", color,
                        "territoryPixels", pixelCount
                        //,
                        //"totalPaints", totalPaints
                ));
            }
        }

        // Sort by territory pixels (descending)
        results.sort((a, b) -> {
            int cmp = Integer.compare(
                    (Integer) b.get("territoryPixels"),
                    (Integer) a.get("territoryPixels")
            );
            if (cmp == 0) {
                // Tiebreaker: total paints
                return Long.compare(
                        (Long) b.get("totalPaints"),
                        (Long) a.get("totalPaints")
                );
            }
            return cmp;
        });

        String winner = results.isEmpty() ? "none" :
                (String) results.get(0).get("teamId");

        return Map.of(
                "winner", winner,
                "results", results
        );
    }
}