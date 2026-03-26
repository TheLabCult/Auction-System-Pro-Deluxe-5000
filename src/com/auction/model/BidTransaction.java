package com.auction.model;
//luu tru thong tin cua 1 lan dat gia
public class BidTransaction extends Entity {
    private User bidder;
    private double bidAmount;
    public BidTransaction(User bidder, double bidAmount) {
        super(); //lay id, createdAt (tgian dat gia)
        this.bidder = bidder;
        this.bidAmount = bidAmount;
    }
    public User getBidder() {
        return bidder;
    }
    public double getBidAmount() {
        return bidAmount;
    }

    @Override
    public String toString() {
        return "[" + getCreatedAt() + "]" + bidder.getUsername() + " bid: " + bidAmount;
    }
}
