package com.auction.shared.dto;

public class BidDTO {

    private long id;           
    private long auctionId;    
    private long bidderId;     
    private String bidderName; 
    private double amount;     
    private String timestamp;  
    private long timestampMillis; // Cột X trong bảng linechart
    private boolean autoBid;   // true nếu BidManager tự động đặt

    public BidDTO() {} 

    // Getters

    public long getId() { return id; }
    public long getAuctionId() { return auctionId; }
    public long getBidderId() { return bidderId; }
    public String getBidderName() { return bidderName; }
    public double getAmount() { return amount; }
    public String getTimestamp() { return timestamp; }

    /*Chia cho 1000 để lấy số giây*/
    public long getTimestampMillis() { return timestampMillis; }
    public boolean isAutoBid() { return autoBid; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setAuctionId(long auctionId) { this.auctionId = auctionId; }
    public void setBidderId(long bidderId) { this.bidderId = bidderId; }
    public void setBidderName(String bidderName) { this.bidderName = bidderName; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public void setTimestampMillis(long timestampMillis) { this.timestampMillis = timestampMillis; }
    public void setAutoBid(boolean autoBid) { this.autoBid = autoBid; }
}
