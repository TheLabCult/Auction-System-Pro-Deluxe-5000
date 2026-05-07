package com.auction.server.observer;

import com.auction.shared.models.Auction;      
import com.auction.shared.models.BidTransaction;

import java.util.Collections;                 
import java.util.Set;                         
import java.util.concurrent.ConcurrentHashMap; 
import java.util.concurrent.ExecutorService;  
import java.util.concurrent.Executors;        

// TODO: comment here
public final class AuctionEventBus {

    private static volatile AuctionEventBus instance;

    private AuctionEventBus() {}

    public static AuctionEventBus getInstance() {
        if (instance == null) {
            synchronized (AuctionEventBus.class) {
                if (instance == null) instance = new AuctionEventBus();
            }
        }
        return instance;
    }

    private final ConcurrentHashMap<Long, Set<AuctionObserver>> watchers =
            new ConcurrentHashMap<>();

    private final ExecutorService notifyPool = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "notify-pool");
        t.setDaemon(true); 
        return t;
    });


    public void subscribe(long auctionId, AuctionObserver observer) {
        watchers.computeIfAbsent(auctionId,
                k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                .add(observer);
    }

    public void unsubscribe(long auctionId, AuctionObserver observer) {
        Set<AuctionObserver> set = watchers.get(auctionId);
        if (set != null) set.remove(observer);
    }

    public void unsubscribeAll(AuctionObserver observer) {
        
        watchers.values().forEach(set -> set.remove(observer));
    }

    public void publishBidPlaced(Auction auction, BidTransaction bid) {
        fan(auction.getId(), obs -> obs.onBidPlaced(auction, bid));
    }

    public void publishAuctionEnded(Auction auction) {
        fan(auction.getId(), obs -> obs.onAuctionEnded(auction));
    }

    public void publishAuctionExtended(Auction auction) {
        fan(auction.getId(), obs -> obs.onAuctionExtended(auction));
    }


    private void fan(long auctionId, ObserverAction action) {
        Set<AuctionObserver> set = watchers.get(auctionId);
        if (set == null || set.isEmpty()) return;
        for (AuctionObserver obs : set) {
            
            notifyPool.submit(() -> {
                try {
                    action.apply(obs);
                } catch (Exception e) {
                    // TODO: here
                }
            });
        }
    }

    
    @FunctionalInterface
    private interface ObserverAction {
        void apply(AuctionObserver obs);
    }
}

