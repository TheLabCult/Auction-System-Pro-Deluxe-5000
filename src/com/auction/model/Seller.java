package com.auction.model;
import java.util.ArrayList;
import java.util.List;
public class Seller extends User {
    private String shopName;
    private List<Item> myItems;
    private double revenue;
    public Seller(String username, String password, String email, String fullName, String shopName) {
        super(username, password, email, fullName);
        this.shopName = shopName;
    }
    @Override
    public void displayRoleInfo() {
        System.out.println("Role: SELLER | Shop: " + shopName);
    }
    public String getShopName() {
        return shopName;
    }
    public void addRevenue(double amount) {
        revenue += amount;
    }
}