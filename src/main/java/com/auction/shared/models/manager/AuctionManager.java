package main.java.com.auction.shared.models.manager;

import main.java.com.auction.shared.models.auction.Auction;

import java.util.HashMap;

public class AuctionManager {
    //constructors
    private static AuctionManager instance;

    private HashMap<String, Auction> auctions;

    private AuctionManager() {
        auctions = new HashMap<>();
    }

    public static AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    //getters
    public HashMap<String, Auction> getAuctions() {
        return auctions;
    }

    //methods
    public void addAuction(Auction auction) { auctions.put(auction.getId(), auction); }
}