package com.auction.shared.models;

import java.time.LocalDateTime; 

/*
File này để lưu cấu hình autobid cho bidder

Khi một Bidder nhấn "Set AutoBid", server sẽ lưu một hàng AutoBid vào một bảng
riêng trong db
BidService đọc tất cả các AutoBid đang hoạt động và đưa chúng vào PriorityQueue
để quyết định ai sẽ auto-bid tiếp và với số tiền là bao nhiêu

Quy tắc:
- 2 auto-bid bằng nhau thì auto-bid có max bid lớn hơn thắng
- Cùng max bid nữa thì auto-bid nào registeredAt sớm hơn đc ưu tiên

Và khi đến giới hạn maxBid thì auto-bid sẽ bị vô hiệu hóa (active = false)
 */

public final class AutoBid extends Entity {

    private long auctionId;       // Áp dụng với auction nào
    private long bidderId;        // Của bidder nào
    private String bidderName;    
    private double maxBid;        
    private double increment;     
    private LocalDateTime registeredAt; 
    private boolean active = true; 

    public AutoBid() {
        super();
        this.registeredAt = LocalDateTime.now();
    }

    @Override
    public void printInfo() {
        System.out.printf("[AUTOBID] id=%d  auction=%d  bidder=%-15s  max=%.2f  inc=%.2f  active=%s%n",
                id, auctionId, bidderName, maxBid, increment, active);
    }

    // Getters / Setters

    public long getAuctionId() { return auctionId; }
    public void setAuctionId(long auctionId) { this.auctionId = auctionId; }

    public long getBidderId() { return bidderId; }
    public void setBidderId(long bidderId) { this.bidderId = bidderId; }

    public String getBidderName() { return bidderName; }
    public void setBidderName(String bidderName) { this.bidderName = bidderName; }

    /* AutoBid sẽ dừng khi đạt đến maxBid, logic ở trong backend */
    public double getMaxBid() { return maxBid; }
    public void setMaxBid(double maxBid) { this.maxBid = maxBid; }

    /* Bước nhảy giá đặt bid */
    public double getIncrement() { return increment; }
    public void setIncrement(double increment) { this.increment = increment; }

    /* Phân định Bid nào sẽ lên nếu cùng maxBid */
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }

    /* Xem AutoBid còn hiệu lực không*/
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}

