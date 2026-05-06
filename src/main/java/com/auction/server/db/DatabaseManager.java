package com.auction.server.db;

import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseManager {

    public static void initializeTables() throws SQLException {
        try {
            Statement stmt = DatabaseConnection.getInstance().getConnection().createStatement();


            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id          TEXT PRIMARY KEY,
                        username    TEXT NOT NULL UNIQUE,
                        password    TEXT NOT NULL,
                        email       TEXT NOT NULL,
                        role        TEXT NOT NULL,
                        active      INTEGER NOT NULL,
                        created_at  TEXT NOT NULL
                    )
            """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS items (
                        id              TEXT PRIMARY KEY,
                        name            TEXT NOT NULL,
                        description     TEXT,
                        category        TEXT NOT NULL,
                        seller_id       TEXT NOT NULL,
                        created_at      TEXT NOT NULL
                        )
            """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS auctions (
                        id              INTEGER PRIMARY KEY AUTOINCREMENT,
                        item_id         INTEGER NOT NULL REFERENCES items(id),
                        starting_price  REAL NOT NULL,
                        current_price   REAL NOT NULL,
                        start_time      TEXT NOT NULL,
                        end_time        TEXT NOT NULL,
                        status          TEXT NOT NULL,
                        seller_id       INTEGER NOT NULL REFERENCES users(id)
                        winner_id       TEXT NOT NULL,
                    )
            """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS bid_transactions (
                        id          TEXT PRIMARY KEY,
                        auction_id  TEXT NOT NULL,
                        bidder_id   TEXT NOT NULL,
                        amount      REAL NOT NULL,
                        is_auto_bid INTEGER NOT NULL DEFAULT 0,
                        created_at  TEXT NOT NULL
                    )
            """);    
            
    
            stmt.close();
            System.out.println("Database tables initialized successfully! Yeah!!!");
        } catch (SQLException e) {
            throw new RuntimeException("Creating tables failed!");
        }
    }
}


/* NHIỆM VỤ CỦA FILE NÀY TRONG HỆ THỐNG ĐẤU GIÁ

Tạo các bảng lưu trữ dữ liệu nếu chưa có, chỉ vậy thôi
*/


/* 
Muốn truyền câu lệnh vào database thì mình cần có một biến kiểu dữ liệu Statement
khi truyền vào database thì mình dùng <tên biến>.execute(<câu lệnh trong này>);
ba dấu ngoặc kép là formatted string, tức là mình để dạng nào
thò thụt đầu dòng như nào thì nó đúng như vâyk
*/
