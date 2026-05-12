package com.auction.server.dao;

import com.auction.server.enums.AuctionStatus;
import com.auction.server.models.Auction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuctionDAO {

    Auction save(Auction auction);

    Optional<Auction> findById(long id);

    List<Auction> findAll();

    List<Auction> findBySellerId(long sellerId);

    List<Auction> findByStatus(AuctionStatus status);

    void updateStatus(long auctionId, AuctionStatus status);

    void updateCurrentPrice(long auctionId, double price, long leadingBidderId);

    void updateEndTime(long auctionId, LocalDateTime newEndTime);

    void updateWinner(long auctionId, long winnerId, AuctionStatus status);
}
