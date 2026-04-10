package main.java.com.auction.shared.models.user;

import main.java.com.auction.shared.models.Entity;

import java.time.*;

abstract public class User extends Entity {
    //constructor
    final String username;
    private String fullName;
    private String email;
    private String password;
    private boolean activeStatus;
    public User(String fullname, String username, String email, String password) {
        this.fullName = fullname;
        this.username = username;
        this.email = email;
        this.password = password;
        this.activeStatus = true;
    }

    //getters
    public String getUsername() { return username; }
    public String getFullName() {return fullName; }
    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }

    //setters
    public void setUsername(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPassword(String password) { this.password = password; }
    public void setActiveStatus(boolean activeStatus) { this.activeStatus = activeStatus; }
}