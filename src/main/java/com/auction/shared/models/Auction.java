package com.auction.shared.models;
import com.auction.shared.enums.*;
import com.auction.shared.interfaces.*;
import com.auction.shared.exceptions.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.time.LocalDateTime;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class Auction extends Entity {
    private Item item; //san pham
    private Seller seller; //ng ban
    private LocalDateTime startTime; //thoi diem bat dau
    private LocalDateTime endTime; //thoi diem ket thuc
    private AuctionStatus status; //trang thai (open /
    private BidTransaction highestBid; //gia cao nhat
    private final List<BidTransaction> bidHistory; //lich su bid
    private int timeLeft;
    private transient List<AuctionObserver> observers;
    private transient ReentrantLock lock;
     //tgian ket thuc
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
        this.timeLeft = (int) ChronoUnit.SECONDS.between(startTime, endTime);
        this.bidHistory = new CopyOnWriteArrayList<>();

        this.observers = new CopyOnWriteArrayList<>();
        this.lock = new ReentrantLock();
    }
    public Auction(Item item, Seller seller, int durationSec) {
        super();
        if (item.getStatus() != ItemStatus.IN_AUCTION) {
            throw new IllegalArgumentException("Item is in another auction or sold");
        }
        this.item = item;
        this.item.setStatus(ItemStatus.IN_AUCTION); //lock item lại
        this.seller = seller;
        this.timeLeft = durationSec;
        this.status = AuctionStatus.OPEN;
        this.bidHistory = new CopyOnWriteArrayList<>();

        this.observers = new CopyOnWriteArrayList<>();
        this.lock = new ReentrantLock();
    }
    public void init() {
        if (this.lock == null) this.lock = new ReentrantLock();
        if (this.observers == null) this.observers = new CopyOnWriteArrayList<>();
    }
    //logic observer
    public void addObserver(AuctionObserver ob) {
        this.observers.add(ob);
    }
    public void removeObserver(AuctionObserver ob) {
        this.observers.remove(ob);
    }
    private void notify(BidTransaction newBid) {
        if (observers != null) for (AuctionObserver ob: observers) ob.onNewBid(this.getId()     , newBid);
    }
    public synchronized boolean placeBid(BidTransaction newBid) throws AuctionClosedException, InvalidBidException, InsufficientFundsException {
        init();
        lock.lock();
        try {
            if (this.status != AuctionStatus.RUNNING) {
                throw new AuctionClosedException("Ended or has not started yet");
            }
            double currentPrice = highestBid != null ? highestBid.getAmount() : item.getStartingPrice();
            if (newBid.getAmount() <= currentPrice) throw new InvalidBidException("Amount must be larger than current price");
            if (newBid.getBidder().getBalance() < newBid.getAmount()) throw new InsufficientFundsException("not enough balance");
            //update data
            this.highestBid = newBid;
            this.bidHistory.add(highestBid);
            //observer
            notify(this.highestBid);
            return true;
        }
        finally {
            lock.unlock();
        }
    }
    public synchronized void closeAuction() {
        init();
        lock.lock();
        try {
            this.status = AuctionStatus.FINISHED;
            if (observers != null) {
                for (AuctionObserver ob: observers) ob.onAuctionEnded(this.getId(), this.highestBid);
                observers.clear();
            }
        }
        finally {
            lock.unlock();
        }
    }
    //public void startTimer() {}
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
    public LocalDateTime getStartTime() {
        return startTime;
    }
    public LocalDateTime getEndTime() {
        return endTime;
    }
    public Seller getSeller() {
        return seller;
    }
    //setter (for admin / auto)
    public void setStatus(AuctionStatus status) {
        this.status = status;
    }
}
