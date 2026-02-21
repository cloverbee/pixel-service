package com.multiverse.pixel.entity;

public class Team {
    private String teamId;
    private String teamName;
    private String color;

    public Team() {}

    public Team(String teamId, String teamName, String color) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.color = color;
    }

    // Getters and setters
    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}