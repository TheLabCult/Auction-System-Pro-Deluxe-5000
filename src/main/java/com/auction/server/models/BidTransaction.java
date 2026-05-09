package com.auction.shared.models;

import java.time.LocalDateTime; 

/*
Bid thủ công hoặc bid tự động đều đc ghi lại bằng cái này

Thuộc tính autobid phân biệt auto và thủ công
- Thủ công: bidder nhấn BID NOW
- Tự động: BidService resolveAutoBids() đặt bid tự động phản hồi lại bid của đối thủ

Quan hệ với Aution:
- Nhiều BidTransaction thuộc 1 Auction 
- Lịch sử được tải bằng BidDAO.findByAuctionId() và được biểu diễn bằng
một bảng linechart trong AuctionDetailController
 */
public final class BidTransaction extends Entity {

    private long auctionId;    
    private long bidderId;     
    private String bidderName; 
    private double amount;     // > currentPrice
    private LocalDateTime timestamp; 
    private boolean autoBid;   // true - đặt bằng autobid

    public BidTransaction() {
        super();
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public void printInfo() {
        System.out.printf("[BID] id=%d  auction=%d  bidder=%-15s  amount=%.2f  auto=%s  at=%s%n",
                id, auctionId, bidderName, amount, autoBid, timestamp);
    }


    // == Getters / setters =========================================

    public long getAuctionId() { return auctionId; }
    public void setAuctionId(long auctionId) { this.auctionId = auctionId; }

    public long getBidderId() { return bidderId; }
    public void setBidderId(long bidderId) { this.bidderId = bidderId; }

    public String getBidderName() { return bidderName; }
    public void setBidderName(String bidderName) { this.bidderName = bidderName; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public boolean isAutoBid() { return autoBid; }
    public void setAutoBid(boolean autoBid) { this.autoBid = autoBid; }
}
