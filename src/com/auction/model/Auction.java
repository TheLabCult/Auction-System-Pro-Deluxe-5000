package com.auction.model;
//prevent concurrency
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
public class Auction extends Entity {
    //định danh, liên kết
    private String auctionID;
    private Item item;
    private User seller;
    //thời gian, trạng thái
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AuctionStatus status;
    //thông tin đấu giá
    private BidTransaction highestBid; //lich su dat gia max
    //CopyOnWriteArrayList đảm bảo thread-safe khi có nhiều bid cùng lúc
    private final List<BidTransaction> bidHistory; //lich su dat gia an toan

    public Auction(Item item, User seller, LocalDateTime startTime, LocalDateTime endTime) {
        super();
        if (item.getStatus() != ItemStatus.IN_AUCTION) {
            throw new IllegalArgumentException("Item is in another auction or sold");
        }
        this.item = item;
        this.item.setStatus(ItemStatus.IN_AUCTION); //khoá item lại
        this.seller = seller;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = AuctionStatus.OPEN;
        this.bidHistory = new CopyOnWriteArrayList<>();
    }
    //đặt giá cốt lõi (thread-safe)
    public synchronized boolean placeBid(User bidder, double amount) {
        //kiem tra ngoai le (phien da dong hay chua?)
        if (this.status != AuctionStatus.RUNNING) {
            System.out.println("Refuse: Inactive Auction");
            return false;
        }
        //kiem tra ngoai le: gia dat co hop le? (> gia hien tai)
        double curPrice = highestBid != null ? highestBid.getBidAmount() : item.getStartingPrice();
        if (curPrice >= amount) {
            System.out.println("Refuse: Auction amount must be greater than current price: " + curPrice);
            return false;
        }
        //logic đóng băng tiền người đặt
        if (!bidder.freezeMoney(amount)) {
            System.out.println("Refuse: Bidder " + bidder.getUsername() + " does not have enough balance");
        }
        //logic hoan lai tien (unfrozen) cho nguoi top 1 hien tai
        if (highestBid != null) {
            User prevLeader = highestBid.getBidder();
            prevLeader.unfreezeMoney(highestBid.getBidAmount());
            System.out.println("Refund " + highestBid.getBidAmount() + " for " + prevLeader.getUsername());
        }
        //update status for next leader
        BidTransaction newBid = new BidTransaction(bidder, amount);
        this.highestBid = newBid;
        this.bidHistory.add(newBid);
        System.out.println("Successfully: " + bidder.getUsername() + " is now current leader with " + newBid.getBidAmount());
        return true;
    }
    //getter
    public Item getItem() {
        return item;
    }
    public AuctionStatus getStatus() {
        return status;
    }

    public BidTransaction getHighestBid() {
        return highestBid;
    }
    public List<BidTransaction> getBidHistory() {
        return bidHistory;
    }
    //setter (for admin / auto)
    public void setStatus(AuctionStatus status) {
        this.status = status;
    }
    //Thông tin tổng quát của một phiên đấu giá
    public void getDetails() {
        System.out.println();
    }
}
