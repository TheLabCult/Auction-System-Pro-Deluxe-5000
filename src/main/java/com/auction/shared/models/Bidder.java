package com.auction.shared.models;

public class Bidder extends User{
    private double balance;
    public Bidder(String name, String password, String email, double balance) {
        super(name, password, email);
        this.balance = balance;
    }
    public Bidder(String id, String name, String password, String email, double balance) {
        super(id, name, password, email);
        this.balance = balance;
    }
    //getter
    public double getBalance() {
        return balance;
    }
    //setter
    public synchronized void addBalance(double amount) {
        this.balance += amount;
    }
    //method
    public synchronized boolean deduct(double amount) {
        if (balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    }
    @Override
    public void getInfo() {
        System.out.println("Role: BIDDER | Username: " + getUsername()+ " | Balance: " + balance);
    }
}
