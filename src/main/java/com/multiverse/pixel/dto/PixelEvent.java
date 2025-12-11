package com.multiverse.pixel.dto;

public class PixelEvent {
    private int x;
    private int y;
    private String color;
    private String userId;

    // Constructors
    public PixelEvent() {}

    public PixelEvent(int x, int y, String color, String userId) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.userId = userId;
    }

    // Getters and Setters
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}