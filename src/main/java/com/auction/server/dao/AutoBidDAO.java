package com.auction.server.dao;

import java.util.List;
import java.util.Optional;

import com.auction.server.models.AutoBid;


public interface AutoBidDAO {

    AutoBid save(AutoBid autoBid);

    List<AutoBid> findActiveByAuctionId(long auctionId);

    Optional<AutoBid> findByAuctionAndBidder(long auctionId, long bidderId);

    void deactivate(long autoBidId);
}
