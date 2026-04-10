package main.java.com.auction.shared.models.auction;

import main.java.com.auction.shared.models.item.*;
import main.java.com.auction.shared.models.user.*;
import main.java.com.auction.shared.enums.AuctionStatus;
import main.java.com.auction.shared.models.Entity;

import java.time.*;
import java.util.*;

public class Auction extends Entity{
    //constructor
    private Item item;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Seller seller;
    private List<BidTransaction> bidHistory;
    private AuctionStatus status;

    public Auction(Seller seller, Item item, LocalDateTime startTime, LocalDateTime endTime) {
        this.seller = seller;
        this.item = item;
        this.startTime = startTime;
        this.endTime = endTime;
        this.bidHistory = new ArrayList<>();
        this.status = AuctionStatus.OPEN;
    }

    //getters
    public Seller getSeller() {
        return seller;
    }
    public LocalDateTime getStartTime() {
        return startTime;
    }
    public LocalDateTime getEndTime() {
        return endTime;
    }

    //setters
    public synchronized void finishAuction() {
        this.status = AuctionStatus.FINISHED;
    }

    //methods
    public synchronized void placeBid(BidTransaction bidTransaction) {
        if (status!= AuctionStatus.OPEN) {
            throw new IllegalArgumentException("Auction is not active.");
        }
        if (bidTransaction.getBidAmount() < bidHistory.get(bidHistory.size()-1).getBidAmount()) {
            throw new IllegalArgumentException("Bid amount must be higher than the previous bid.");
        }
        bidHistory.add(bidTransaction);
    }
}