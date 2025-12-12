package com.multiverse.pixel.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;  // ← Add this import
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;  // ← Add this import

@JsonIgnoreProperties(ignoreUnknown = true)  // ← Add this annotation
public class GameState {

    public enum State {
        WAITING, ACTIVE, FINISHED
    }

    private State state;
    private Long startTime; // Unix timestamp
    private Long endTime;   // Unix timestamp
    private Integer durationSeconds;

    public GameState() {
        this.state = State.WAITING;
    }
    @JsonIgnore
    public boolean isActive() {
        if (state != State.ACTIVE) return false;
        if (endTime == null) return true;
        return System.currentTimeMillis() / 1000 < endTime;
    }
    @JsonIgnore
    public long getRemainingSeconds() {
        if (!isActive()) return 0;
        if (endTime == null) return 0;
        return endTime - (System.currentTimeMillis() / 1000);
    }

    // Getters and setters
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }

    public Long getStartTime() { return startTime; }
    public void setStartTime(Long startTime) { this.startTime = startTime; }

    public Long getEndTime() { return endTime; }
    public void setEndTime(Long endTime) { this.endTime = endTime; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }
}