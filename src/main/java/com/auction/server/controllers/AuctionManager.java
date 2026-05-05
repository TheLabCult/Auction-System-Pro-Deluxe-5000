package com.auction.server.controllers;
import com.auction.shared.models.*;
import com.auction.shared.enums.*;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.*;
import java.util.concurrent.*;


public class AuctionManager {
    private static AuctionManager instance;
    private Map<String, Auction> auctions;
    private ScheduledExecutorService scheduler;
    private AuctionManager() {
        auctions = new ConcurrentHashMap<>();
        startTimer();
    }
    public static synchronized AuctionManager getInstance() {
        if (instance == null) instance = new AuctionManager();
        return instance;
    }
    //logic chuyen trang thai open -> running -> finished -> paid/canceled
    private void startTimer() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            updateStatus();
        }, 0, 1, TimeUnit.SECONDS);
    }
    //auto status transition
    private void updateStatus() {
        LocalDateTime now = LocalDateTime.now();
        for (Auction au: auctions.values()) {
            //open -> running
            if (au.getStatus() == AuctionStatus.OPEN && !now.isBefore(au.getStartTime())) {
                au.setStatus(AuctionStatus.RUNNING);
            }
            //running -> finished
            else if (au.getStatus() == AuctionStatus.RUNNING && !now.isBefore(au.getEndTime())) {
                resolveWinner(au);
            }
        }
    }
    private void resolveWinner(Auction au) {
        au.closeAuction();
        BidTransaction winningBid = au.getHighestBid();
        if (winningBid != null) {
            //TODO: database update ho cai nguoi thang
        }
        else {
            //TODO: ko co ai tra gia, trang phien, ko ban dc
        }
    }
    //manual status transition
    //to paid
    public boolean markPaid(String auctionId) {
        Auction au = auctions.get(auctionId);
        if (au == null || au.getStatus() != AuctionStatus.FINISHED) return false;
        BidTransaction winningBid = au.getHighestBid();
        if (winningBid == null) return false;
        Bidder winner = winningBid.getBidder();
        Seller seller = au.getSeller();
        double finalPrice = winningBid.getAmount();
        //transaction
        //try to deduct
        if (winner.deduct(finalPrice)) {
            seller.addBalance(finalPrice);
            au.setStatus(AuctionStatus.PAID);
            //TODO: log xuong transaction
            return true;
        }
        else {
            cancelAuction(auctionId);
            //giao dich that bai, co the phat bidder/ cancel,...
            return false;
        }
    }
    public boolean cancelAuction(String auctionId) {
        Auction au = auctions.get(auctionId);
        if (au != null && (au.getStatus() != AuctionStatus.OPEN || au.getStatus() != AuctionStatus.RUNNING)) {
            au.setStatus(AuctionStatus.CANCELED);
            // TODO: update xuong sql
            return true;
        }
        return false;
    }
    public Auction createAuction(Item item, Seller seller, LocalDateTime startTime, LocalDateTime endTime) {
        Auction newAuction = new Auction(item, seller, startTime, endTime);
        auctions.put(newAuction.getId(), newAuction);
        return newAuction;
    }
    public void addAuction(Auction auction) {
        auctions.put(auction.getId(), auction);
    }
    public Auction getAuctionById(String auctionId) {
        return auctions.get(auctionId);
    }
    public Map<String, Auction> getAllAuctions() {
        return auctions;
    }
    public List<Auction> getActiveAuctions() {
        List<Auction> activeList = new ArrayList<>();
        for (Auction auction : auctions.values()) {
            if (auction.getStatus() == AuctionStatus.OPEN || auction.getStatus() == AuctionStatus.RUNNING) {
                activeList.add(auction);
            }
        }
        return activeList;
    }
}
