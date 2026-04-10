package com.auction.server.controllers;
import com.auction.shared.models.*;
    import java.util.concurrent.ConcurrentHashMap;
import java.util.*;
public class AuctionManager {
    private static AuctionManager instance;
    private Map<String, Auction> activeAuctions;
    private AuctionManager() {
        activeAuctions = new ConcurrentHashMap<>();
    }
    public static AuctionManager getInstance() {
        if (instance == null) instance = new AuctionManager();
        return instance;
    }
    public void addAuction(Auction auction) {
        activeAuctions.put(auction.getId(), auction);
    }

    public Auction getAuctionById(String auctionId) {
        return activeAuctions.get(auctionId);
    }

    public Map<String, Auction> getAllAuctions() {
        return activeAuctions;
    }
}
