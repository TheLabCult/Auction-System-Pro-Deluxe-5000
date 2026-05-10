package com.auction.server.dao;

import java.util.List;

import com.auction.server.models.BidTransaction;


public interface BidDAO {

    BidTransaction save(BidTransaction bid);

    List<BidTransaction> findByAuctionId(long auctionId);
}
