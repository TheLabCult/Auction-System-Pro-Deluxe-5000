import java.time.LocalDateTime;

class Product {
    private int id;
    private String name;
    private double startPrice;
    private double currentPrice;
    private String highestBidder;
    private boolean isClosed;
    private LocalDateTime auctionEndTime;
    private LocalDateTime auctionStartTime;
    private LocalDateTime previousBidTime;

    public Product(int id, String name, double startPrice, LocalDateTime auctionStartTime, LocalDateTime auctionEndTime) {
        this.id = id;
        this.name = name;
        this.startPrice = startPrice;
        this.currentPrice = startPrice;
        this.auctionStartTime = auctionStartTime;
        this.auctionEndTime = auctionEndTime;
        this.isClosed = false;
    }

    // Đặt giá
    public synchronized boolean placeBid(String bidderName, double amount) {
            LocalDateTime now = LocalDateTime.now();
        if (!isClosed && amount > currentPrice) {
            this.currentPrice = amount;
            this.highestBidder = bidderName;
            this.previousBidTime = now;
            return true;
        }
        return false;
    }
    
    // Đóng đấu giá
    public void closeAuction() {
        this.isClosed = true;
    }

    public void displayInfo() {
        System.out.println("Product ID: " + id);
        System.out.println("Name: " + name);
        System.out.println("Start Price: " + startPrice);
        System.out.println("Current Price: " + currentPrice);
        System.out.println("Highest Bidder: " + highestBidder);
        System.out.println("Previous Bid Time: " + previousBidTime);
        System.out.println("Auction Duration: " + auctionStartTime + " to " + auctionEndTime);
        System.out.println("Auction Status: " + (isClosed ? "Closed" : "Open"));
    }
}