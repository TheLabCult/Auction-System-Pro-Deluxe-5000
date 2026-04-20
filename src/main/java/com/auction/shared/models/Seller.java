package com.auction.shared.models;

import com.auction.shared.enums.Role;

import java.time.*;
import java.util.UUID;

public class Seller extends User {
    private Role role;

    private Seller(String id, String username, String password, String email, LocalDateTime createdAt) {
        super(id, username, password, email, createdAt);
        this.role = Role.SELLER;
    } 

    public static Seller createNew(String username, String password, String email) {
        return new Seller(
            UUID.randomUUID().toString(), 
            username, 
            password, 
            email, 
            LocalDateTime.now());
    } 

    public static Seller fromDatabase(String id, String username, String password, String email, LocalDateTime createdAt) {
        return new Seller(id, username, password, email, createdAt);
    }
    public String getRoleString() { return this.role.name(); }
    
    public void cancelAuction() {}
}
