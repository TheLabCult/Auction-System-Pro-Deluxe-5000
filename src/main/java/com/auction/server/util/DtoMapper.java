package com.auction.server.util;


import com.auction.shared.dto.AuctionDTO;
import com.auction.shared.dto.BidDTO;
import com.auction.shared.dto.ItemDTO;
import com.auction.shared.dto.UserDTO;


import com.auction.server.models.*;

import java.time.ZoneId;                        
import java.time.format.DateTimeFormatter;      


public final class DtoMapper {

    private DtoMapper() {} 

    
    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // User -> UserDTO

    public static UserDTO toDto(User u) {
        return new UserDTO(u.getId(), u.getUsername(), u.getEmail(),
                u.getRole().name(), 
                u.isActive());
    }

    // Item -> ItemDTO

    public static ItemDTO toDto(Item item) {
        ItemDTO dto = new ItemDTO();

        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setCategory(item.getCategory().name()); // enum -> string
        dto.setSellerId(item.getSellerId());
        dto.setSellerName(item.getSellerName());
        if (item.getCreatedAt() != null) dto.setCreatedAt(FMT.format(item.getCreatedAt()));
        
        return dto;
    }

    // Auction -> AuctionDTO

    public static AuctionDTO toDto(Auction a) {
        AuctionDTO dto = new AuctionDTO();

        dto.setId(a.getId());
        dto.setItem(a.getItem() != null ? toDto(a.getItem()) : null); 
        dto.setStartingPrice(a.getStartingPrice());
        dto.setCurrentPrice(a.getCurrentPrice());
        dto.setStartTime(FMT.format(a.getStartTime()));
        dto.setEndTime(FMT.format(a.getEndTime()));
        dto.setStatus(a.getStatus().name()); // enum -> string
        dto.setSellerId(a.getSellerId());
        dto.setSellerName(a.getSellerName());
        dto.setWinnerId(a.getLeadingBidderId());       // null if no bids yet
        dto.setWinnerName(a.getLeadingBidderName());   // null if no bids yet
        if (a.getCreatedAt() != null) dto.setCreatedAt(FMT.format(a.getCreatedAt()));

        return dto;
    }

    // BidTransaction -> BidDTO

    public static BidDTO toDto(BidTransaction bt) {
        BidDTO dto = new BidDTO();

        dto.setId(bt.getId());
        dto.setAuctionId(bt.getAuctionId());
        dto.setBidderId(bt.getBidderId());
        dto.setBidderName(bt.getBidderName());
        dto.setAmount(bt.getAmount());
        if (bt.getTimestamp() != null) {
            dto.setTimestamp(FMT.format(bt.getTimestamp())); // string
            // LocalDateTime -> ZonedDateTime -> Instant -> epoch millis.
            // ZoneId.systemDefault() uses the server's local timezone.
            dto.setTimestampMillis(
                    bt.getTimestamp()
                      .atZone(ZoneId.systemDefault())
                      .toInstant()
                      .toEpochMilli());
        }
        dto.setAutoBid(bt.isAutoBid());

        return dto;
    }
}
