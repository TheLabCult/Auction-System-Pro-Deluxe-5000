package com.auction.model;

import java.util.ArrayList;
import java.util.List;

public class User extends Entity {
    private String username;
    private String password;
    private String email;
    private String fullName;
    private boolean isActive; //state (admin can lock)
    private double balance;
    private double frozen; //tiền bị giữ khi đấu giá
    private String shopName;
    private List<Item> myItems; // các sản phẩm
    public User(String username, String password, String email, String fullName, String shopName) {
        super();
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.isActive = true;
        this.shopName = shopName;
        this.myItems = new ArrayList<>();
    }
    // public abstract void displayRoleInfo();
    //getter
    public String getUsername() {
        return this.username;
    }
    public String getFullName() {
        return this.fullName;
    }
    public String getEmail() {
        return this.email;
    }
    public double getBalance() {
        return this.balance;
    }
    public String getShopName() {
        return shopName;
    }
    public List<Item> getMyItems() { //get list of items for displaying (GUI)
        return myItems;
    }
    //setter
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public boolean isActive() {
        return isActive;
    }
    public void addBalance(double amount) {
        balance += amount;
    }
    public void setActive(boolean state) {
        isActive = state;
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
    public void addItem(Item item) {
        myItems.add(item);
        System.out.println("Added " + item.getName());
    }
    public boolean removeItem(Item item) {
        if (myItems.remove(item)) {
            System.out.println("Removed " + item.getName());
            return true;
        }
        return false;
    }
    public void addRevenue(double amount) {
        balance += amount;
        System.out.println("Shop " + shopName + " received revenue of " + amount);
    }
    //khi đấu giá, trừ balance, cộng frozen
    public boolean freezeMoney(double amount) {
        if (balance < amount) return false;
        balance -= amount;
        frozen += amount;
        return true;
    }
    //khi có người thắng với bid cao hơn -> refund
    public void unfreezeMoney(double amount) {
        frozen -= amount;
        balance += amount;
    }
    //khi phiên kết thúc, thắng
    public void payForWonAuction(double amount) {
        frozen -= amount;
    }
//    public void setPassword(String password) {
//        this.password = password;
//    }
}

