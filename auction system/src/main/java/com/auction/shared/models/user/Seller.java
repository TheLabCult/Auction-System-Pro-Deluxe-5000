package main.java.com.auction.shared.models.user;

import main.java.com.auction.shared.models.item.Item;

import java.util.*;

public class Seller extends User {
    //constructors
    private String shopName;
    private double balance;
    private ArrayList<Item> itemList;
    public Seller(String fullname, String username, String email, String password, String shopName) {
        super(fullname, username, email, password);
        this.shopName = shopName;
        this.balance = 0;
        itemList = new ArrayList<>();
    }
    public Seller(String fullname, String username, String email, String password, String shopName, double balance) {
        super(fullname, username, email, password);
        this.shopName = shopName;
        this.balance = balance;
    }

    //getters
    public String getShopName() { return shopName; }
    public double getBalance() { return balance; }

    //setters
    public void setShopName(String shopName) { this.shopName = shopName; }
    public void addBalance(double balance) { this.balance = balance; }
    public void addItem(Item item) { this.itemList.add(item); }
}