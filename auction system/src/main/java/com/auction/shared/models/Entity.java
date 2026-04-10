package main.java.com.auction.shared.models;
import java.util.*;
import java.time.*;
public abstract class Entity {
    private String id;
    private final LocalDateTime createdAt;
    public Entity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }
    //getter
    public String getId() {
        return id;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    //setter
    public void setId(String id) {
        this.id = id;
    }
}