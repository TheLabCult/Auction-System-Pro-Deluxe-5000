package com.auction.server.dao;

import com.auction.shared.models.AutoBid;

import java.util.List;
import java.util.Optional;


public interface AutoBidDAO {

    AutoBid save(AutoBid autoBid);

    List<AutoBid> findActiveByAuctionId(long auctionId);

    Optional<AutoBid> findByAuctionAndBidder(long auctionId, long bidderId);

    void deactivate(long autoBidId);
}
