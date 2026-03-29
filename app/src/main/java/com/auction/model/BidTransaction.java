package com.auction.model;
//luu tru thong tin cua 1 lan dat gia
import java.time.LocalDateTime;
public class BidTransaction extends Entity {
    private StandardUser bidder;
    private double bidAmount;
    public BidTransaction(StandardUser bidder, double bidAmount) {
        super(); //lay id, createdAt (tgian dat gia)
        this.bidder = bidder;
        this.bidAmount = bidAmount;
    }
    public StandardUser getBidder() {
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
