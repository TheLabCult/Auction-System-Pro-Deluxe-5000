package com.auction.shared.interfaces;
import com.auction.shared.models.*;
public interface AuctionObserver {
    void onNewBid(String auctionId, BidTransaction newBid);
    void onAuctionEnded(String auctionId, BidTransaction winningBid);
}
