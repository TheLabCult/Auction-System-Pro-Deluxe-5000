package com.auction.shared.models;

import java.time.*;

public abstract class Entity {
    private String id;
    private final LocalDateTime createdAt;

    protected Entity(String id, LocalDateTime createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    
}
