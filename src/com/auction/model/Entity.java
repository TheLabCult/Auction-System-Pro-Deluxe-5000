package com.auction.model;
import java.time.LocalDateTime;
import java.util.UUID;
public abstract class Entity {
    private final String id;
    private final LocalDateTime createdAt;
    public Entity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }
    public String getId() {
        return this.id;
    }
    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}