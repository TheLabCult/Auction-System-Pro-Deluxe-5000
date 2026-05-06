package com.auction.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.auction.server.db.*;
import com.auction.shared.models.*;

public class ItemDAO {
    private Connection conn;

    public ItemDAO() throws SQLException {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    public boolean save(Item item) {
        String sql = "INSERT INTO items (id, name, description, starting_price, category, seller_id VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);

            stmt.setString(1, item.getId());
            stmt.setString(2, item.getName());
            stmt.setString(3, item.getDescription());
            stmt.setDouble(4, item.getStartingPrice());
            stmt.setString(5, item.getCategory());
            stmt.setDouble(6, item.getSellerId());

            stmt.executeUpdate();

            return true;

        } catch (SQLException e) {
            System.err.println("UserDAO save failed!" + e.getMessage());
            return false;
        } finally { stmt.close(); }
    }
    
    
}
