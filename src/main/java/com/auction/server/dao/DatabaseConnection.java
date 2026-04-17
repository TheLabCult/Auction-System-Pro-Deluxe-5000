package com.auction.server.dao;

/* Ý nghĩa của file này trong hệ thống đấu giá

Đây là cổng kết nối giữa server và SQLite file ở trên ổ đĩa auction.db
Khi khởi động chương trình bình thường nếu không có DatabaseConnection, các object được
tạo ở trên RAM, khi tắt chương trình sẽ mất đi, lần sau khởi động lại không còn gì cả,
mới toanh

Thế nên sẽ cần một file lưu trữ trong ổ đĩa, và file này là cầu nối giữa server và 
file lưu trên ổ đâix đó

File này có hai việc: 
1. Mở hoặc tạo file auction.db (nếu chưa có) khi khởi động server
2. Tạo ra các bảng SQLite để lưu trữ dữ liệu (nếu chưa có).

Các class DAO - data access object sẽ gọi DatabaseConnection.getInstance().getConnection()
để lấy kết nối. các class đó không kết nối trực tiếp, mà sẽ kết nối thông qua class này.

Lựa chọn Design Pát từn: Singleton
-> đảm bảo chỉ có 1 instance của class này tồn tại
để tránh mở 2 cổng đến DB write đồng thời lên DB*/


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;



public class DatabaseConnection {

    
    private static final String DB_URL = "jdbc:sqlite:auction.db";

    // Singleton - chỉ 1 object để kết nối database
    private static DatabaseConnection instance;

    private Connection connection;

    private DatabaseConnection() {
        try {
            connection = DriverManager.getConnection(DB_URL);

            // PRAGMA... - nhiều thread read từ db ok, nhưng chỉ 1 thread write xuống db
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
        
        Statement stmt = connection.createStatement();


        stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id          TEXT PRIMARY KEY,
                    username    TEXT NOT NULL UNIQUE,
                    password    TEXT NOT NULL,
                    email       TEXT NOT NULL,
                    role        TEXT NOT NULL
                )
        """);




        stmt.execute("""
                CREATE TABLE IF NOT EXISTS items (
                    id              TEXT PRIMARY KEY,
                    name            TEXT NOT NULL,
                    description     TEXT,
                    starting_price  REAL NOT NULL,
                    category        TEXT NOT NULL,
                    seller_id       TEXT NOT NULL,
                    FOREIGN KEY (seller_id) REFERENCES users(id)
                    )
        """);



        stmt.execute("""
                CREATE TABLE IF NOT EXISTS auctions (
                    id              TEXT PRIMARY KEY,
                    item_id         TEXT NOT NULL,
                    current_price   REAL NOT NULL,
                    start_time      TEXT NOT NULL,
                    end_time        TEXT NOT NULL,
                    status          TEXT NOT NULL,
                    winner_id       TEXT NOT NULL,
                    FOREIGN KEY (item_id)    REFERENCES items(id),
                    FOREIGN KEY (winner_id)  REFERENCES users(id)
                )
        """);



        stmt.execute("""
                CREATE TABLE IF NOT EXISTS bid_transactions (
                    id          TEXT PRIMARY KEY,
                    auction_id  TEXT NOT NULL,
                    bidder_id   TEXT NOT NULL,
                    amount      REAL NOT NULL,
                    bid_time    TEXT NOT NULL,
                    FOREIGN KEY (auction_id) REFERENCES auctions(id),
                    FOREIGN KEY (bidder_id)  REFERENCES users(id)
                )
        """);    
    
        stmt.close();
        System.out.println("Database tables initialized successfully! Yeah!!!");
    }
}
