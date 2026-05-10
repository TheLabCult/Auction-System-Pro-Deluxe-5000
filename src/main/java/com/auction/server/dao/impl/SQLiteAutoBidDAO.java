package com.auction.server.dao.impl;

import com.auction.server.dao.AutoBidDAO;      
import com.auction.server.db.DatabaseConnection;
import com.auction.server.models.AutoBid;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
Điểm chính ở đây là UPSERT trong save():
ON CONFLICT(...) DO...

Tức nếu một Bidder gọi 2 lần autobid trong 1 phiên, lần gọi thứ hai sẽ cập 
nhật hàng đã có they vì một hàng mới
UNIQUE -> chỉ một autobid config cho mỗi phiên
deactivate() chỉ đưa active về 0 thay vì xóa đi autobid, bảo tồn log
*/

public final class SQLiteAutoBidDAO implements AutoBidDAO {

    private Connection conn() { return DatabaseConnection.getInstance().getConnection(); }

    @Override
    public synchronized AutoBid save(AutoBid ab) {
        // UPSERT: INSERT hàng, nhưng nếu đã có thì update, không thêm nữa
        String sql = """
            INSERT INTO auto_bids (auction_id, bidder_id, max_bid, increment, registered_at, active)
            VALUES (?, ?, ?, ?, ?, 1)
            ON CONFLICT(auction_id, bidder_id) DO UPDATE SET
                max_bid       = excluded.max_bid,
                increment     = excluded.increment,
                registered_at = excluded.registered_at,
                active        = 1
        """;
        // 'excluded' - các giá trị mới được đưa vào để update.
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, ab.getAuctionId());
            ps.setLong(2, ab.getBidderId());
            ps.setDouble(3, ab.getMaxBid());
            ps.setDouble(4, ab.getIncrement());
            ps.setString(5, ab.getRegisteredAt().toString());
            ps.executeUpdate();
            try (ResultSet keys = conn().createStatement().executeQuery("SELECT last_insert_rowid()")) {
                if (keys.next()) ab.setId(keys.getLong(1));
            }
            return ab;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save auto-bid", e);
        }
    }

    @Override
    public synchronized List<AutoBid> findActiveByAuctionId(long auctionId) {
        String sql = """
            SELECT ab.*, u.username AS bidder_name
            FROM auto_bids ab JOIN users u ON ab.bidder_id = u.id
            WHERE ab.auction_id = ? AND ab.active = 1
            ORDER BY ab.registered_at ASC
        """;
        // active = 1 filter: các autobid hết hiệu lực thì sẽ vô hình đối với logic.
        // ASC thứ tự: đăng kí autobid ở thời gian sớm hơn thì được ưu tiên.
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, auctionId);
            return mapList(ps.executeQuery());
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public synchronized Optional<AutoBid> findByAuctionAndBidder(long auctionId, long bidderId) {
        String sql = """
            SELECT ab.*, u.username AS bidder_name
            FROM auto_bids ab JOIN users u ON ab.bidder_id = u.id
            WHERE ab.auction_id = ? AND ab.bidder_id = ?
        """;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, auctionId);
            ps.setLong(2, bidderId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return Optional.empty();
            return Optional.of(map(rs));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public synchronized void deactivate(long autoBidId) {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE auto_bids SET active = 0 WHERE id = ?")) {
            ps.setLong(1, autoBidId);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private List<AutoBid> mapList(ResultSet rs) throws SQLException {
        List<AutoBid> list = new ArrayList<>();
        while (rs.next()) list.add(map(rs));
        return list;
    }

    private AutoBid map(ResultSet rs) throws SQLException {
        AutoBid ab = new AutoBid();
        ab.setId(rs.getLong("id"));
        ab.setAuctionId(rs.getLong("auction_id"));
        ab.setBidderId(rs.getLong("bidder_id"));
        ab.setBidderName(rs.getString("bidder_name")); // from JOIN alias
        ab.setMaxBid(rs.getDouble("max_bid"));
        ab.setIncrement(rs.getDouble("increment"));
        ab.setRegisteredAt(LocalDateTime.parse(rs.getString("registered_at")));
        ab.setActive(rs.getInt("active") == 1);
        return ab;
    }
}
