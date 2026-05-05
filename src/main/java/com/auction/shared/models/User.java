package com.auction.shared.models;

public abstract class User extends Entity {
    private final String username;
    private String password;
    private String email;
    private boolean isActive;
    //private String fullName;
    public User(String username, String password, String email) {
        super();
        this.username = username;
        this.password = password;
        this.email = email;
        //this.fullname = "";
        this.isActive = true;
    }
    public User(String id, String username, String password, String email) {
        super(id);
        this.username = username;
        this.password = password;
        this.email = email;
    }
    //method
    public abstract void getInfo();
    //getter
    public String getEmail() {
        return email;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() { return password; }
    public boolean isActive() {
        return isActive;
    }
    //setter
    public void setEmail(String email) {
        this.email = email;
    }
    public void setActive(boolean active) {
        isActive = active;
    }
    public boolean changePassword(String oldPassword, String newPassword) {
        if (oldPassword.equals(this.password)) {
            this.password = newPassword;
            return true;
        }
        return false;
    }
    public boolean verifyPassword(String password) {
        return this.password.equals(password);
    }
}
