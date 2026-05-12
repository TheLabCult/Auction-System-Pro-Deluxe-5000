package com.auction.shared.dto;

public class AuctionDTO {
    private long id;            
    private ItemDTO item;      
    private double startingPrice;
    private double currentPrice; 
    private String startTime;   
    private String endTime;     
    private String status;      
    private long sellerId;      
    private String sellerName;  
    private Long winnerId;      // null nếu không ai bid
    private String winnerName;  
    private String createdAt;   

    public AuctionDTO() {} 

    // Getters

    public long getId() { return id; }
    public ItemDTO getItem() { return item; }
    public double getStartingPrice() { return startingPrice; }
    public double getCurrentPrice() { return currentPrice; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getStatus() { return status; }
    public long getSellerId() { return sellerId; }
    public String getSellerName() { return sellerName; }
    public Long getWinnerId() { return winnerId; }
    public String getWinnerName() { return winnerName; }
    public String getCreatedAt() { return createdAt; }

    // Setters (needed by Gson)
    
    public void setId(long id) { this.id = id; }
    public void setItem(ItemDTO item) { this.item = item; }
    public void setStartingPrice(double startingPrice) { this.startingPrice = startingPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setStatus(String status) { this.status = status; }
    public void setSellerId(long sellerId) { this.sellerId = sellerId; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public void setWinnerId(Long winnerId) { this.winnerId = winnerId; }
    public void setWinnerName(String winnerName) { this.winnerName = winnerName; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
