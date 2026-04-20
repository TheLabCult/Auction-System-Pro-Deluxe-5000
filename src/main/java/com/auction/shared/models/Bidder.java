package com.auction.shared.models;


import com.auction.shared.enums.*;

import java.time.LocalDateTime;
import java.util.UUID;

public class Bidder extends User{
    private Role role;

    private Bidder(String id, String username, String password, String email, LocalDateTime createdAt) {
        super(id, username, password, email, createdAt);
        this.role = Role.BIDDER;
    }

    public static Bidder createNew(String username, String password, String email) {
        return new Bidder(
            UUID.randomUUID().toString(),
            username,
            password,
            email,
            LocalDateTime.now();
        );
    }

    public static Bidder fromDatabase(String id, String username, String password, String email, LocalDateTime createdAt) {
        return new Bidder(id, username, password, email, createdAt);
    }

    
    public String getRoleString() { return this.role.name(); }
}
