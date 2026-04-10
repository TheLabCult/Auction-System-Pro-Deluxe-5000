package main.java.com.auction.shared.models.user;

public class Bidder extends User {
    //constructor
    private double balance;
    private double frozen;

    public Bidder(String username, String fullName, String email, String password) {
        super(username, fullName, email, password);
        this.balance = 0;
        this.frozen = 0;
    }
    public Bidder(String fullname, String username, String email, String password, double balance) {
        super( fullname, username, email, password);
        this.balance = balance;
        this.frozen = 0;
    }

    //getters
    public double getBalance() {
        return balance;
    }

    //setters
    public void addBalance(double balance) {
        this.balance += balance;
    }

    //Bid functions

    //Freeze money when place bid
    public void freezeMoney(double balance) {
        this.balance -= balance;
        this.frozen += balance;
    }

    //Unfreeze money when outbid
    public void unfreezeMoney(double balance) {
        this.balance += balance;
        this.frozen -= balance;
    }

    //Unfreeze money when win bid
    public void unfreezeWin(double balance) {
        this.frozen -= balance;
    }
}