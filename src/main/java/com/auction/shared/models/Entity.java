package com.auction.shared.models;
import java.io.Serializable;
import java.util.*;
import java.time.*;
public abstract class Entity implements Serializable {
    private String id;
    public Entity() {
        this.id = UUID.randomUUID().toString();
    }
    protected Entity(String id) {
        this.id = id;
    }
    //getter
    public String getId() {
        return id;
    }
    //setter
    public void setId(String id) {
        this.id = id;
    }
}
