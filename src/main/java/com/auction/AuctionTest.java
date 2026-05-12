package com.auction;

import com.auction.server.enums.AuctionStatus;
import com.auction.server.exceptions.AuctionClosedException;
import com.auction.server.exceptions.InsufficientFundsException;
import com.auction.server.exceptions.InvalidBidException;
import com.auction.server.models.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class

AuctionTest {

    private Auction auction;
    private Bidder bidder1;
    private Bidder bidder2;
    @BeforeEach
    public void setUp() {
        Seller seller = new Seller("seller", "123", "seller@gmail.com");
        Item laptop = new Electronics("MacBook", 1000, "M3 Pro", 12);

        // Tạo phiên đấu giá bắt đầu ở quá khứ và kết thúc ở tương lai
        LocalDateTime start = LocalDateTime.now().minusMinutes(5);
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        auction = new Auction(laptop, seller, start, end);
        auction.setStatus(AuctionStatus.RUNNING);

        bidder1 = new Bidder("bidder1", "123", "bidder1@mail.com", 5000);
        bidder2 = new Bidder("bidder2", "123", "bidder2@mail.com", 1200);
    }

    @Test
    public void testValidBid() throws Exception {
        BidTransaction validBid = new BidTransaction(bidder1, 1200);
        boolean result = auction.placeBid(validBid);

        assertTrue(result, "Đặt giá hợp lệ phải trả về true");
        assertEquals(1100, auction.getHighestBid().getAmount());
        assertEquals("bidder1", auction.getHighestBid().getBidder().getUsername());
    }

    @Test
    public void testInvalidBid_LowerThanCurrent() {
        // bidder1 đặt 1500
        assertDoesNotThrow(() -> auction.placeBid(new BidTransaction(bidder1, 1500)));

        // Bidder2 cố tình đặt 1200 (thấp hơn)
        BidTransaction invalidBid = new BidTransaction(bidder2, 1200);

        // Xác nhận hệ thống ném đúng loại lỗi InvalidBidException
        Exception exception = assertThrows(InvalidBidException.class, () -> {
            auction.placeBid(invalidBid);
        });

        assertTrue(exception.getMessage().contains("phải cao hơn"));
    }

    @Test
    public void testAuctionClosedException() {
        auction.setStatus(AuctionStatus.FINISHED); // Giả lập đóng phiên

        BidTransaction lateBid = new BidTransaction(bidder1, 2000);

        assertThrows(AuctionClosedException.class, () -> {
            auction.placeBid(lateBid);
        });
    }

    @org.testng.annotations.Test
    public void testInsufficientFundsException() {
        // bidder2 chỉ có 1200 trong tài khoản nhưng đặt giá 2000
        BidTransaction overBudgetBid = new BidTransaction(bidder2, 2000);
        assertThrows(InsufficientFundsException.class, () -> {
            auction.placeBid(overBudgetBid);
        });
    }
}