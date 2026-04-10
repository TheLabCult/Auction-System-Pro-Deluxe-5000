package com.auction.shared.models;

public class Bidder extends User{
    private double balance;
    private double frozen;
    public Bidder(String name, String password, String email, double balance) {
        super(name, password, email);
        this.balance = balance;
        this.frozen = 0;
    }
    //getter
    public double getBalance() {
        return balance;
    }
    //setter
    public void setBalance(double balance) {
        this.balance = balance;
    }
    public void addBalance(double amount) {
        this.balance += amount;
    }
    //method
    //freeze money khi bid
    public boolean freeze(double amount) {
        if (this.balance >= amount) {
            this.balance -= amount;
            frozen += amount;
            return true;
        }
        return false;
    }
    public void unfreeze(double amount) {
        frozen -= amount;
        balance += amount;
    }
    public void payForWonAuction(double amount) {
        if (frozen >= amount) frozen -= amount;
        else throw new IllegalStateException("not enough frozen balance");
    }
    @Override
    public void getInfo() {
        System.out.println("Role: BIDDER | Username: " + getUsername()+ " | Balance: " + balance);
    }
}
