package com.auction.server.models;

import java.time.LocalDateTime;

public abstract class Entity {
    // Khi đưa vào database, db sẽ tự gán cho id theo thứ tự tăng dần 
    protected long id;
    protected LocalDateTime createdAt;

    protected Entity() {
        // Tất cả subclass sẽ có một thuộc tính là thời điểm được tạo ra
        this.createdAt = LocalDateTime.now();
    }


    // Abstract contract ----------------------------------------------------

    public abstract void printInfo();


    // Getters / Setters ----------------------------------------------------

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
