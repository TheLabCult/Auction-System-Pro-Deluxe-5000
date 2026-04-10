package com.auction.shared.models;

import java.time.LocalDateTime;
//a bid transaction
public class BidTransaction extends Entity {
    private Bidder bidder;
    private double bidAmount;
    public BidTransaction(Bidder bidder, double bidAmount) {
        super();
        this.bidder = bidder;
        this.bidAmount = bidAmount;
    }
    public Bidder getBidder() {
        return bidder;
    }
    public double getBidAmount() {
        return bidAmount;
    }
}
