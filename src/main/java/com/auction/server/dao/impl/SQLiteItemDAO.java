package com.auction.server.dao.impl;


import com.auction.server.dao.ItemDAO;         
import com.auction.server.db.DatabaseConnection;
import com.auction.server.enums.ItemCategory;
import com.auction.server.factory.ItemFactory;
import com.auction.server.models.Item;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
SELECT queries kết nối bảng users để lấy username của seller
Vì bảng của items không có cột username của seller, chỉ có seller_id
Dùng lệnh JOIN để tránh việc cần câu lệnh thứ hai
ItemFactory được gọi để khởi tạo class đứng với category
 */

public final class SQLiteItemDAO implements ItemDAO {

    private Connection conn() { return DatabaseConnection.getInstance().getConnection(); }

    @Override
    public synchronized Item save(Item item) {
        String sql = """
            INSERT INTO items (name, description, category, seller_id, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getDescription());
            ps.setString(3, item.getCategory().name()); 
            ps.setLong(4, item.getSellerId());
            ps.setString(5, item.getCreatedAt().toString());
            ps.executeUpdate();
            try (ResultSet keys = conn().createStatement().executeQuery("SELECT last_insert_rowid()")) {
                if (keys.next()) item.setId(keys.getLong(1));
            }
            return item;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save item", e);
        }
    }

    @Override
    public synchronized Optional<Item> findById(long id) {

        String sql = """
            SELECT i.*, u.username AS seller_name
            FROM items i JOIN users u ON i.seller_id = u.id
            WHERE i.id = ?
        """;

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return Optional.empty();
            return Optional.of(map(rs));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public synchronized List<Item> findBySellerId(long sellerId) {
        String sql = """
            SELECT i.*, u.username AS seller_name
            FROM items i JOIN users u ON i.seller_id = u.id
            WHERE i.seller_id = ?
            ORDER BY i.id DESC
        """;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, sellerId);
            return mapList(ps.executeQuery());
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private List<Item> mapList(ResultSet rs) throws SQLException {
        List<Item> list = new ArrayList<>();
        while (rs.next()) list.add(map(rs));
        return list;
    }

    /* Đưa ResultSet về đối tượng item, nếu không có ItemFactory thì ở đây sẽ
    có if/else hoặc switch. Factory dùng để ẩn đi switch */
    private Item map(ResultSet rs) throws SQLException {
        ItemCategory category = ItemCategory.valueOf(rs.getString("category"));
        Item item = ItemFactory.create(category); 
        item.setId(rs.getLong("id"));
        item.setName(rs.getString("name"));
        item.setDescription(rs.getString("description"));
        item.setSellerId(rs.getLong("seller_id"));
        item.setSellerName(rs.getString("seller_name")); 
        item.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
        return item;
    }
}

