package com.auction.shared.network;

import com.auction.shared.dto.*;

import java.util.List;


public final class Requests {

 
    private Requests() {}

    // Authentication

    public static final class LoginRequest {
        public String username; 
        public String password; 

        public LoginRequest() {}
        public LoginRequest(String u, String p) { username = u; password = p; }
    }

    public static final class RegisterRequest {
        public String username;
        public String password;
        public String email;   
        public String role;    

        public RegisterRequest() {}
        public RegisterRequest(String u, String p, String e, String r) {
            username = u; password = p; email = e; role = r;
        }
    }

    // Bidding

    public static final class PlaceBidRequest {
        public long auctionId; 
        public double amount;  

        public PlaceBidRequest() {}
        public PlaceBidRequest(long auctionId, double amount) {
            this.auctionId = auctionId; this.amount = amount;
        }
    }

    public static final class SetAutoBidRequest {
        public long auctionId; 
        public double maxBid;  
        public double increment;

        public SetAutoBidRequest() {}
        public SetAutoBidRequest(long auctionId, double maxBid, double increment) {
            this.auctionId = auctionId; this.maxBid = maxBid; this.increment = increment;
        }
    }

    // Item management

    public static final class CreateItemRequest {
        public String name;        
        public String description; 
        public String category;    // "ELECTRONICS", "ART", or "VEHICLE"

        public CreateItemRequest() {}
    }

    public static final class CreateAuctionRequest {
        public long itemId;        
        public double startingPrice;
        public String startTime;   
        public String endTime;    

        public CreateAuctionRequest() {}
    }

    // Auction subscription

    public static final class WatchAuctionRequest {
        public long auctionId; 

        public WatchAuctionRequest() {}
        public WatchAuctionRequest(long id) { this.auctionId = id; }
    }

    public static final class GetAuctionDetailRequest {
        public long auctionId;

        public GetAuctionDetailRequest() {}
        public GetAuctionDetailRequest(long id) { this.auctionId = id; }
    }

    public static final class GetBidHistoryRequest {
        public long auctionId;

        public GetBidHistoryRequest() {}
        public GetBidHistoryRequest(long id) { this.auctionId = id; }
    }

    public static final class CancelAuctionRequest {
        public long auctionId;

        public CancelAuctionRequest() {}
        public CancelAuctionRequest(long id) { this.auctionId = id; }
    }

    public static final class BanUserRequest {
        public long userId;

        public BanUserRequest() {}
        public BanUserRequest(long id) { this.userId = id; }
    }
}
