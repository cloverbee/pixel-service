package com.multiverse.pixel.entity;


import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;

@Table("pixel_history")
public class PixelHistoryEntity {

    @PrimaryKeyColumn(name = "x", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private int x;

    @PrimaryKeyColumn(name = "y", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private int y;

    @PrimaryKeyColumn(name = "event_time", ordinal = 2, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private Instant eventTime;

    @Column("user_id")
    private String userId;

    @Column("color")
    private String color;

    // Constructors
    public PixelHistoryEntity() {}

    public PixelHistoryEntity(int x, int y, Instant eventTime, String userId, String color) {
        this.x = x;
        this.y = y;
        this.eventTime = eventTime;
        this.userId = userId;
        this.color = color;
    }

    // Getters and Setters
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public Instant getEventTime() { return eventTime; }
    public void setEventTime(Instant eventTime) { this.eventTime = eventTime; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}