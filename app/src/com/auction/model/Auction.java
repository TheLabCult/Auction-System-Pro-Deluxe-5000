package com.auction.model;
//prevent concurrency
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
public class Auction extends Entity {
    //định danh, liên kết
    private Item item;
    private StandardUser seller;
    //thời gian, trạng thái
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AuctionStatus status;
    //thông tin đấu giá
    private BidTransaction highestBid; //lich su dat gia max
    //CopyOnWriteArrayList đảm bảo thread-sàe khi có nhiều bid cùng lúc
    private final List<BidTransaction> bidHistory; //lich su dat gia an toan

    public Auction(Item item, StandardUser seller, LocalDateTime startTime, LocalDateTime endTime) {
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
    public synchronized boolean placeBid(StandardUser bidder, double amount) {
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




//        //logic đóng băng tiền người đặt
//        if (!bidder.freezeMoney(amount)) {
//            System.out.println("Refuse: Bidder " + bidder.getUsername() + " does not have enough balance");
//        }
//        //logic hoan lai tien (unfrozen) cho nguoi top 1 hien tai
//        if (highestBid != null) {
//            Bidder prevLeader = highestBid.getBidder();
//            prevLeader.unfreezeMoney(highestBid.getBidAmount());
//            System.out.println("Refund " + highestBid.getBidAmount() + " for " + prevLeader.getUsername());
//        }

        /* Ở trong eBay aunction, người đấu giá thắng rồi mới trả tiền
        * không phải là đặt bid là phải trả tiền luôn
        * balance - số dư trên eBay không phản ánh đúng khả nănng tài chính của họ
        * vì giống như ví Shoppee, cần mua thì người ta mới chuyênr từ tài khoản ngân hàng vào thôi
        * nếu bắt buộc phải có một khoản frozen thì rất bất tiện
        * đặt mua nhiều bid khác nhau lại chuyển phải thêm một khoản vào ví
        * tăng bid lại phải thêm khoản nữa, rất nhiều thao tác tốn thời gian*/



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
}
