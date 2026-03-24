package com.auction.model;
public class Bidder extends User {
    private double balance, frozen;
    public Bidder(String name, String password, String email, String fullName, double balance) {
        super(name, password, email, fullName);
        this.balance = balance;
        this.frozen = 0;
    }
    @Override
    public void displayRoleInfo() {
        System.out.println("Role: BIDDER | Username: " + getUsername() + " | Balance: " + balance);
    }
    public double getBalance() {
        return balance;
    }
    public void addBalance(double amount) {
        balance += amount;
    }
    public boolean freezeMoney(double amount) {
        if (balance < amount) return false;
        balance -= amount;
        frozen += amount;
        return true;
    }
    public void unfreezeMoney(double amount) {
        frozen -= amount;
        balance += amount;
    }
}
