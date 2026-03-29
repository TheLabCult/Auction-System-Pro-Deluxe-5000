package com.auction.model;

/* standardUser có khả năng của cả Bidder và Seller
 Nếu chia ra 2 loại tài khoản khác nhau thì người dùng muốn vừa
 đấu giá vừa bán lại phải đổi qua đổi lại 2 loại tài khoản khác nhau.
 */

import java.util.ArrayList;
import java.util.List;

public class StandardUser extends User {

    private List<Item> myItems;
    private double balance;

    public StandardUser(String username, String password, String email, String fullName, double balance){
        super(username, password, email, fullName);
        this.balance = balance;
        this.myItems = new ArrayList<>();
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
        this.balance += amount;
        System.out.println("Received revenue of " + amount);
    }

    public List<Item> getMyItems() { //get list of items for displaying (GUI)
        return myItems;
    }

    // Getter & Setter
    public double getBalance() {
        return this.balance;
    }
    public void addBalance(double amount) { this.balance += amount; }



    public String displayRoleInfo(){
        return "Role: Standard User";
    }
}
