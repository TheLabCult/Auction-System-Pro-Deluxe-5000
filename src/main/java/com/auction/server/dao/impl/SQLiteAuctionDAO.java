package com.auction.server.dao.impl;

import com.auction.server.dao.AuctionDAO;      
import com.auction.server.db.DatabaseConnection;
import com.auction.server.enums.AuctionStatus;
import com.auction.server.enums.ItemCategory;
import com.auction.server.factory.ItemFactory;
import com.auction.server.models.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
Đây là DAO phức tạp nhất
query SELECT cần hợp 3 bảng lại với nhau: 
auctions -> items -> users (seller)
auctions -> users (LEFT JOIN vì winner có thể null)

Điều này sẽ tạo ra một hàng lớn (ghép từ các bảng khác nhau)
chứa toàn bộ dữ liệu đủ để tái tạo Auction và Item trong Auction đó
*/
public final class SQLiteAuctionDAO implements AuctionDAO {

    private Connection conn() { return DatabaseConnection.getInstance().getConnection(); }

    private static final String SELECT_BASE = """
        SELECT
            a.*,
            i.name          AS item_name,
            i.description   AS item_desc,
            i.category      AS item_category,
            i.seller_id     AS item_seller_id,
            i.created_at    AS item_created,
            s.username      AS seller_name,
            w.username      AS winner_name
        FROM auctions a
        JOIN items   i ON a.item_id   = i.id
        JOIN users   s ON a.seller_id = s.id
        LEFT JOIN users w ON a.winner_id = w.id
    """;


    @Override
    public synchronized Auction save(Auction auction) {
        
        String sql = """
            INSERT INTO auctions
                (item_id, starting_price, current_price, start_time, end_time, status, seller_id, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, auction.getItem().getId());
            ps.setDouble(2, auction.getStartingPrice());
            ps.setDouble(3, auction.getCurrentPrice());
            ps.setString(4, auction.getStartTime().toString());
            ps.setString(5, auction.getEndTime().toString());
            ps.setString(6, auction.getStatus().name()); // "OPEN" or "RUNNING"
            ps.setLong(7, auction.getSellerId());
            ps.setString(8, auction.getCreatedAt().toString());
            ps.executeUpdate();
            try (ResultSet keys = conn().createStatement().executeQuery("SELECT last_insert_rowid()")) {
                if (keys.next()) auction.setId(keys.getLong(1));
            }
            return auction;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save auction", e);
        }
    }

    @Override
    public synchronized Optional<Auction> findById(long id) {
        String sql = SELECT_BASE + " WHERE a.id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return Optional.empty();
            return Optional.of(map(rs));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public synchronized List<Auction> findAll() {
        String sql = SELECT_BASE + " ORDER BY a.id DESC"; 
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            return mapList(ps.executeQuery());
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public synchronized List<Auction> findBySellerId(long sellerId) {
        String sql = SELECT_BASE + " WHERE a.seller_id = ? ORDER BY a.id DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, sellerId);
            return mapList(ps.executeQuery());
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public synchronized List<Auction> findByStatus(AuctionStatus status) {
        String sql = SELECT_BASE + " WHERE a.status = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, status.name());
            return mapList(ps.executeQuery());
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public synchronized void updateStatus(long auctionId, AuctionStatus status) {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE auctions SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setLong(2, auctionId);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public synchronized void updateCurrentPrice(long auctionId, double price, long leadingBidderId) {
        // Both price and winner are always updated together — they are logically atomic.
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE auctions SET current_price = ?, winner_id = ? WHERE id = ?")) {
            ps.setDouble(1, price);
            ps.setLong(2, leadingBidderId);
            ps.setLong(3, auctionId);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public synchronized void updateEndTime(long auctionId, LocalDateTime newEndTime) {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE auctions SET end_time = ? WHERE id = ?")) {
            ps.setString(1, newEndTime.toString()); // ISO-8601 with T separator
            ps.setLong(2, auctionId);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public synchronized void updateWinner(long auctionId, long winnerId, AuctionStatus status) {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE auctions SET winner_id = ?, status = ? WHERE id = ?")) {
            ps.setLong(1, winnerId);
            ps.setString(2, status.name());
            ps.setLong(3, auctionId);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    

    private List<Auction> mapList(ResultSet rs) throws SQLException {
        List<Auction> list = new ArrayList<>();
        while (rs.next()) list.add(map(rs));
        return list;
    }


    private Auction map(ResultSet rs) throws SQLException {
        Auction a = new Auction();
        a.setId(rs.getLong("id"));
        a.setStartingPrice(rs.getDouble("starting_price"));
        a.setCurrentPrice(rs.getDouble("current_price"));
        a.setStartTime(LocalDateTime.parse(rs.getString("start_time")));
        a.setEndTime(LocalDateTime.parse(rs.getString("end_time")));
        a.setStatus(AuctionStatus.valueOf(rs.getString("status")));
        a.setSellerId(rs.getLong("seller_id"));
        a.setSellerName(rs.getString("seller_name"));
        a.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));

        
        long winnerId = rs.getLong("winner_id");
        if (!rs.wasNull()) { 
            a.setLeadingBidderId(winnerId);
            a.setLeadingBidderName(rs.getString("winner_name"));
        }


        ItemCategory cat = ItemCategory.valueOf(rs.getString("item_category"));
        Item item = ItemFactory.create(cat); // Factory Method
        item.setId(rs.getLong("item_id"));
        item.setName(rs.getString("item_name"));
        item.setDescription(rs.getString("item_desc"));
        item.setSellerId(rs.getLong("item_seller_id"));
        item.setSellerName(rs.getString("seller_name")); 
        item.setCreatedAt(LocalDateTime.parse(rs.getString("item_created")));
        a.setItem(item);

        return a;
    }
}
