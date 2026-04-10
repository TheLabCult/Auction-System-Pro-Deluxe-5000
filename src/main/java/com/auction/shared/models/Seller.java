package com.auction.shared.models;
import java.util.*;


public class Seller extends User {
    //private String shopname;
    private List<Item> items;
    private double balance;
    public Seller(String username, String password, String email) {
        super(username, password, email);
        items = new ArrayList<>();
    }
    //getter
    public double getBalance() { return balance; }
    public List<Item> getItems() {return items; }
    //setter
    public void addBalance(double amount) {
        balance += amount;
    }
    //method
    public void cancelAuction() {}
    public void addItem(Item item) {
        items.add(item);
    }
    public boolean removeItem(Item item) {
        return items.remove(item);
    }

    @Override
    public void getInfo() {
        System.out.println("Role: Seller | Username: " + getUsername()+ " | Balance: " + balance);
    }
}
