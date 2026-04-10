package com.auction.shared.models;
import com.auction.shared.enums.*;
import java.util.*;
import java.time.LocalDateTime;
import java.util.concurrent.CopyOnWriteArrayList;

public class Auction extends Entity {
    private Item item;
    private Seller seller;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AuctionStatus status;
    private BidTransaction highestBid;
    private final List<BidTransaction> bidHistory;
    //private final List<AuctionObserver> observers;
    private int timeLeft;
    public Auction(Item item, Seller seller, LocalDateTime startTime, LocalDateTime endTime) {
        super();
        if (item.getStatus() != ItemStatus.IN_AUCTION) {
            throw new IllegalArgumentException("Item is in another auction or sold");
        }
        this.item = item;
        this.item.setStatus(ItemStatus.IN_AUCTION); //khoá item lại
        this.seller = seller;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = AuctionStatus.OPEN;
        this.bidHistory = new CopyOnWriteArrayList<>();
    }
    public synchronized void placeBid() {

    }
    public synchronized void closeAuction() {

    }
    //getter
    public Item getItem() {
        return item;
    }
    public AuctionStatus getStatus() {
        return status;
    }
    public int getTimeLeft() { return timeLeft; }
    public BidTransaction getHighestBid() {
        return highestBid;
    }
    public List<BidTransaction> getBidHistory() {
        return bidHistory;
    }
    //setter (for admin / auto)
    public void setStatus(AuctionStatus status) {
        this.status = status;
    }
}
