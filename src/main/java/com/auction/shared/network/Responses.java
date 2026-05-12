package com.auction.shared.network;

import com.auction.shared.dto.*;

import java.util.List; // Java's generic ordered list, used for batch response payloads


public final class Responses {

    private Responses() {} 

    

    public static final class ErrorResponse {
        public String message; 

        public ErrorResponse() {}
        public ErrorResponse(String msg) { this.message = msg; }
    }

    // Auction list responses

    public static final class AuctionsResponse {
        public List<AuctionDTO> auctions; 

        public AuctionsResponse() {}
        public AuctionsResponse(List<AuctionDTO> auctions) { this.auctions = auctions; }
    }

    public static final class ItemsResponse {
        public List<ItemDTO> items;

        public ItemsResponse() {}
        public ItemsResponse(List<ItemDTO> items) { this.items = items; }
    }

    public static final class BidHistoryResponse {
        public long auctionId;  
        public List<BidDTO> bids;

        public BidHistoryResponse() {}
        public BidHistoryResponse(long auctionId, List<BidDTO> bids) {
            this.auctionId = auctionId; this.bids = bids;
        }
    }

    public static final class UsersResponse {
        public List<UserDTO> users;

        public UsersResponse() {}
        public UsersResponse(List<UserDTO> users) { this.users = users; }
    }

    // Bidding responses

    public static final class BidResponse {
        public BidDTO bid;       
        public AuctionDTO auction;

        public BidResponse() {}
        public BidResponse(BidDTO bid, AuctionDTO auction) {
            this.bid = bid; this.auction = auction;
        }
    }

    public static final class AuctionExtendedNotice {
        public long auctionId;   
        public String newEndTime; 

        public AuctionExtendedNotice() {}
        public AuctionExtendedNotice(long auctionId, String newEndTime) {
            this.auctionId = auctionId; this.newEndTime = newEndTime;
        }
    }

    public static final class AutoBidResponse {
        public long auctionId;  
        public double maxBid;   
        public double increment; 

        public AutoBidResponse() {}
        public AutoBidResponse(long auctionId, double maxBid, double increment) {
            this.auctionId = auctionId; this.maxBid = maxBid; this.increment = increment;
        }
    }
}
