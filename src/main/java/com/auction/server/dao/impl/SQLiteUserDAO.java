package com.auction.server.dao.impl;

import com.auction.server.dao.UserDAO;         
import com.auction.server.db.*;
import com.auction.server.enums.*;
import com.auction.server.factory.UserFactory;
import com.auction.server.models.*;

import java.sql.*;                             
import java.time.LocalDateTime;                
import java.util.ArrayList;                    
import java.util.List;                         
import java.util.Optional;                     

/*
Tất cả public method đều synchronized, chỉ hỗ trợ 1 luồng thao tác trên db
 */

public final class SQLiteUserDAO implements UserDAO {

    private Connection conn() { return DatabaseConnection.getInstance().getConnection(); }

    @Override
    public synchronized User save(User user) {
        String sql = """
            INSERT INTO users (username, password, email, role, active, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn().prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getRole().name()); 
            ps.setInt(5, user.isActive() ? 1 : 0);  // SQLite không có boolean -> dùng 0/1
            ps.setString(6, user.getCreatedAt().toString()); 

            ps.executeUpdate();

            try (ResultSet keys = conn().createStatement().executeQuery("SELECT last_insert_rowid()")) {
                if (keys.next()) user.setId(keys.getLong(1)); // store the DB-assigned id
            }
            return user;
        } catch (SQLException e) {
            // Dùng RuntimeException để hàm gọi không lên SQLException
            throw new RuntimeException("Failed to save user: " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized Optional<User> findById(long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            return Optional.ofNullable(mapSingle(ps.executeQuery())); // null → empty Optional
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public synchronized Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, username);
            return Optional.ofNullable(mapSingle(ps.executeQuery()));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public synchronized List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY id";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            return mapList(ps.executeQuery());
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public synchronized void updateActive(long userId, boolean active) {
        String sql = "UPDATE users SET active = ? WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, active ? 1 : 0);
            ps.setLong(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    // ResultSet sẽ phải được tái tạo lại thành đối tượng khi lấy ra từ db

    // Đọc 1 hàng, trả lại null nếu không có
    private User mapSingle(ResultSet rs) throws SQLException {
        if (!rs.next()) return null;
        return map(rs);
    }

    // Đọc tất cả hàng đưa váo 1 danh sách
    private List<User> mapList(ResultSet rs) throws SQLException {
        List<User> list = new ArrayList<>();
        while (rs.next()) list.add(map(rs));
        return list;
    }

    /* Chuyển đổi ResultSet - một hàng lấy ra từ db thành một User hoàn chỉnh */
    private User map(ResultSet rs) throws SQLException {
        UserRole role = UserRole.valueOf(rs.getString("role")); // "BIDDER" → BIDDER enum
        User user = UserFactory.create(role);
        
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setActive(rs.getInt("active") == 1); // 1 -> true, 0 -> false.
        user.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
        return user;
    }
}
