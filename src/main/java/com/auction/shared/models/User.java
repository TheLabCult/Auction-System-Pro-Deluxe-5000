package com.auction.shared.models;

import java.time.LocalDateTime;

public abstract class User extends Entity {
    private String username;
    private String password;
    private String email;
    private double balance;

    protected User(String id, String username, String password, String email, LocalDateTime createdAt) {
        super(id, createdAt);
        this.username = username;
        this.password = password;
        this.email = email;
        this.balance = 0;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getEmail()    { return email; }
    public double getBalance()  { return balance; }
    public abstract String getRoleString();

    public void updateBalance(double amount)      { this.balance += amount; }
}
