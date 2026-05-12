package com.auction.server.dao.impl;

import com.auction.server.dao.BidDAO;
import com.auction.server.db.*;
import com.auction.server.models.BidTransaction;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/* bid_transactions - chỉ thêm, không sửa hay xóa, ez */
public final class SQLiteBidDAO implements BidDAO {

    private Connection conn() { return DatabaseConnection.getInstance().getConnection(); }

    @Override
    public synchronized BidTransaction save(BidTransaction bid) {
        String sql = """
            INSERT INTO bid_transactions (auction_id, bidder_id, amount, is_auto_bid, created_at)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, bid.getAuctionId());
            ps.setLong(2, bid.getBidderId());
            ps.setDouble(3, bid.getAmount());
            ps.setInt(4, bid.isAutoBid() ? 1 : 0); 
            ps.setString(5, bid.getTimestamp().toString()); 
            ps.executeUpdate();
            try (ResultSet keys = conn().createStatement().executeQuery("SELECT last_insert_rowid()")) {
                if (keys.next()) bid.setId(keys.getLong(1));
            }
            return bid;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save bid", e);
        }
    }

    @Override
    public synchronized List<BidTransaction> findByAuctionId(long auctionId) {
        String sql = """
            SELECT bt.*, u.username AS bidder_name
            FROM bid_transactions bt
            JOIN users u ON bt.bidder_id = u.id
            WHERE bt.auction_id = ?
            ORDER BY bt.created_at ASC
        """;
        
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, auctionId);
            ResultSet rs = ps.executeQuery();
            List<BidTransaction> list = new ArrayList<>();
            while (rs.next()) {
                BidTransaction bt = new BidTransaction();
                bt.setId(rs.getLong("id"));
                bt.setAuctionId(rs.getLong("auction_id"));
                bt.setBidderId(rs.getLong("bidder_id"));
                bt.setBidderName(rs.getString("bidder_name")); // from JOIN alias
                bt.setAmount(rs.getDouble("amount"));
                bt.setAutoBid(rs.getInt("is_auto_bid") == 1); // integer → boolean
                bt.setTimestamp(LocalDateTime.parse(rs.getString("created_at")));
                list.add(bt);
            }
            return list;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
