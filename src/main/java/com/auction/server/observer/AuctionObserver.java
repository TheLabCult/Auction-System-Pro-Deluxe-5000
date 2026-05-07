package com.auction.server.observer;

import com.auction.shared.models.Auction;
import com.auction.shared.models.BidTransaction;

public interface AuctionObserver {

    void onBidPlaced(Auction auction, BidTransaction bid);

    void onAuctionEnded(Auction auction);

    void onAuctionExtended(Auction auction);
}
