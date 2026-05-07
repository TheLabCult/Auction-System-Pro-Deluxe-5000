package com.auction.shared.models;

public class Admin extends User{
    public Admin(String username, String password, String email) {
        super(username, password, email);
    }
    @Override
    public void getInfo() {
        System.out.println("Role: Admin | Username: " + getUsername());
    }
}
