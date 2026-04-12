package com.auction.server.dao;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;



public class DatabaseConnection {

    
    private static final String DB_URL = "jdbc:sqlite:auction.db";

    // Singleton - chỉ 1 object để kết nối database - sẽ comment giải thích thêm sau
    private static DatabaseConnection instance;

    private Connection connection;

    private DatabaseConnection() {
        try {
            connection = DriverManager.getConnection(DB_URL);

            // PRAGMA... - nhiều thread read từ db, 1 thread write xuống db
            connection.createStatement().execute("PRAGMA journal_mode=WAL;");

            initializeTables(); // Tạo bảng, method này ở dưới

            System.out.println("[DB] Connected to SQLite: " + DB_URL);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }


    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }


    // Các class DAO khác sẽ gọi cái này, cái này không phải cho file này gọi
    public Connection getConnection() {
        return connection;
    }

    
    private void initializeTables() throws SQLException {
        // Cái này từ từ
    }
}
