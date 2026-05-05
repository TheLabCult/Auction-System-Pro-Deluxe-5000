package com.auction.shared.models;

import java.time.LocalDateTime;
//a bid transaction
public class BidTransaction extends Entity {
    private Bidder bidder;
    private double amount;
    private LocalDateTime timestamp;
    public BidTransaction(Bidder bidder, double bidAmount) {
        super();
        this.bidder = bidder;
        this.amount = bidAmount;
        this.timestamp = LocalDateTime.now();
    }
    public Bidder getBidder() {
        return bidder;
    }
    public double getAmount() {
        return amount;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
