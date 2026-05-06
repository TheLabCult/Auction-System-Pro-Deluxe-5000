package com.auction.server.dao;

import com.auction.shared.models.BidTransaction;

import java.util.List;


public interface BidDAO {

    BidTransaction save(BidTransaction bid);

    List<BidTransaction> findByAuctionId(long auctionId);
}
