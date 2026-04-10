package main.java.com.auction.shared.models.auction;

import main.java.com.auction.shared.models.user.Bidder;
import main.java.com.auction.shared.models.Entity;
import java.time.LocalDateTime;

public class BidTransaction extends Entity {
    private Bidder bidder;
    private Auction auction;
    private double bidAmount;
    private LocalDateTime timestamp;

    public BidTransaction(Bidder bidder, Auction auction, double bidAmount) {
        this.bidder = bidder;
        this.auction = auction;
        this.bidAmount = bidAmount;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public Bidder getBidder() { return bidder; }
    public Auction getAuction() { return auction; }
    public double getBidAmount() { return bidAmount; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
