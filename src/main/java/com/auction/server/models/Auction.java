package com.auction.server.models;

import java.time.LocalDateTime;

import com.auction.server.enums.AuctionStatus; 

/* 
Concurrency:
- Một số thuộc tính là volatile, tức khi BidService ghi currentPrice trong ReetrantLock
các thread khác luôn thấy giá trị mới nhất - không phải cache trong CPU
- volatile vẫn cần ReetrantLock, vì nó chỉ đảm bảo tính nhìn thấy, vẫn cần check bởi lock
- Vòng đời: 
    1. OPEN - có startTime trong tương lai, chưa nhận Bid
    2. RUNNING - đến startTime, bắt đầu nhận bid
    3. FINISHED - đến endTime, quyết định người thắng
    4. PAID - đánh dấu đã thanh toán - việc thanh toán sẽ không diễn ra trên nền 
    tảng này mà sẽ thực hiện qua các kênh ngoài như ngân hàng, bitcoin... 
 */

public class Auction extends Entity {

    private Item item;                         
    private double startingPrice;              
    private volatile double currentPrice;      
    private LocalDateTime startTime;           
    private volatile LocalDateTime endTime;    
    private volatile AuctionStatus status;     
    private long sellerId;                     
    private String sellerName;                 
    private volatile Long leadingBidderId;     
    private volatile String leadingBidderName; 

    public Auction() { super(); }

    @Override
    public void printInfo() {
        System.out.printf("[AUCTION] id=%d  item=%-25s  price=%.2f  status=%s  ends=%s%n",
                id, item != null ? item.getName() : "N/A",
                currentPrice, status, endTime);
    }

    /*
    Chỉ khi running mới nhận bid, check trước khi đặt bid
     */
    public boolean isActive() {
        return status == AuctionStatus.RUNNING;
    }

    // Getters / setters

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    public double getStartingPrice() { return startingPrice; }
    public void setStartingPrice(double startingPrice) { this.startingPrice = startingPrice; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    /*BidService có thể thông qua setEndTime để kéo dài bid - anti sniping */
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public AuctionStatus getStatus() { return status; }
    public void setStatus(AuctionStatus status) { this.status = status; }

    public long getSellerId() { return sellerId; }
    public void setSellerId(long sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    /* Chưa có bid thì null, sau khi auction kết thúc thì đây là người thắng */
    public Long getLeadingBidderId() { return leadingBidderId; }
    public void setLeadingBidderId(Long leadingBidderId) { this.leadingBidderId = leadingBidderId; }

    /* Cũng giống trên, nhưng có thể đc gọi để hiển thị người dẫn đầu nữa */
    public String getLeadingBidderName() { return leadingBidderName; }
    public void setLeadingBidderName(String name) { this.leadingBidderName = name; }
}
